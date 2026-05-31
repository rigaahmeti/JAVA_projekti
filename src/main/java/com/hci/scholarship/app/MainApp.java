package com.hci.scholarship.app;

import app.SessionManager;
import com.hci.scholarship.db.Database;
import com.hci.scholarship.model.ScholarshipApplication;
import com.hci.scholarship.repository.ApplicationRepository;
import com.hci.scholarship.repository.UserRepository;
import com.hci.scholarship.service.LocalizationService;
import com.hci.scholarship.service.ScoringService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MainApp extends Application {
    private final LocalizationService lang = new LocalizationService();
    private final ApplicationRepository repository = new ApplicationRepository();
    private final UserRepository userRepository = new UserRepository();
    private final ScoringService scoringService = new ScoringService();
    private BorderPane root;
    private Label statusBar;
    private TableView<ScholarshipApplication> table;
    private UserRole currentRole;
    private String currentUserName;
    private Map<javafx.scene.Node, Label> fieldErrors = new LinkedHashMap<>();

    private enum UserRole {
        STUDENT, ADMIN
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        Database.initialize();
        root = new BorderPane();
        root.getStyleClass().add("root");
        statusBar = new Label(lang.get("status.ready"));
        showLogin(stage);

        Rectangle2D visibleScreen = Screen.getPrimary().getVisualBounds();
        double sceneWidth = Math.min(1280, visibleScreen.getWidth() - 48);
        double sceneHeight = Math.min(840, visibleScreen.getHeight() - 48);
        Scene scene = new Scene(root, sceneWidth, sceneHeight);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        configureShortcuts(scene, stage);
        stage.setTitle(lang.get("app.title"));
        stage.setScene(scene);
        stage.setMinWidth(980);
        stage.setMinHeight(680);
        stage.setMaxWidth(visibleScreen.getWidth());
        stage.setMaxHeight(visibleScreen.getHeight());
        stage.setX(visibleScreen.getMinX() + (visibleScreen.getWidth() - sceneWidth) / 2);
        stage.setY(visibleScreen.getMinY() + (visibleScreen.getHeight() - sceneHeight) / 2);
        stage.setOnCloseRequest(e -> Platform.exit());
        stage.show();
    }

    private void rebuildLayout(Stage stage) {
        if (currentRole == null) {
            showLogin(stage);
            return;
        }
        root.setTop(new VBox(createMenuBar(stage), createToolbar(stage)));
        root.setCenter(currentRole == UserRole.ADMIN ? createDashboardView() : createStudentHomeView());
        root.setBottom(createStatusBar());
    }

    private MenuBar createMenuBar(Stage stage) {
        Menu file = new Menu(lang.get("menu.file"));
        if (currentRole == UserRole.STUDENT) {
            MenuItem criteria = new MenuItem(lang.get("menu.criteria"));
            criteria.setOnAction(e -> root.setCenter(createCriteriaView()));
            MenuItem newApp = new MenuItem(lang.get("menu.new"));
            newApp.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
            newApp.setOnAction(e -> showApplicationForm());
            file.getItems().addAll(criteria, newApp);
        } else {
            MenuItem applications = new MenuItem(lang.get("menu.applications"));
            applications.setAccelerator(new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN));
            applications.setOnAction(e -> showApplicationsTable());
            MenuItem dashboard = new MenuItem(lang.get("menu.dashboard"));
            dashboard.setAccelerator(new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN));
            dashboard.setOnAction(e -> root.setCenter(createDashboardView()));
            MenuItem database = new MenuItem(lang.get("menu.database"));
            database.setAccelerator(new KeyCodeCombination(KeyCode.B, KeyCombination.CONTROL_DOWN));
            database.setOnAction(e -> showDatabaseWindow(stage));
            file.getItems().addAll(applications, dashboard, database);
        }
        MenuItem logout = new MenuItem(lang.get("menu.logout"));
        logout.setOnAction(e -> logout(stage));
        MenuItem exit = new MenuItem(lang.get("menu.exit"));
        exit.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));
        exit.setOnAction(e -> closeApplication(stage));
        file.getItems().addAll(new SeparatorMenuItem(), logout, exit);

        Menu language = new Menu(lang.get("menu.language"));
        MenuItem albanian = new MenuItem("Shqip");
        albanian.setOnAction(e -> { lang.setLanguage("sq"); stage.setTitle(lang.get("app.title")); rebuildLayout(stage); });
        MenuItem english = new MenuItem("English");
        english.setOnAction(e -> { lang.setLanguage("en"); stage.setTitle(lang.get("app.title")); rebuildLayout(stage); });
        language.getItems().addAll(albanian, english);

        Menu help = new Menu(lang.get("menu.help"));
        MenuItem helpItem = new MenuItem(lang.get("menu.openHelp"));
        helpItem.setAccelerator(new KeyCodeCombination(KeyCode.F1));
        helpItem.setOnAction(e -> showHelp());
        help.getItems().add(helpItem);
        return new MenuBar(file, language, help);
    }

    private ToolBar createToolbar(Stage stage) {
        Button logoutBtn = new Button(lang.get("toolbar.logout"));
        logoutBtn.setOnAction(e -> logout(stage));
        Button helpBtn = new Button(lang.get("toolbar.help"));
        helpBtn.setOnAction(e -> showHelp());
        if (currentRole == UserRole.STUDENT) {
            Button criteriaBtn = new Button(lang.get("toolbar.criteria"));
            criteriaBtn.setOnAction(e -> root.setCenter(createCriteriaView()));
            Button newBtn = new Button(lang.get("toolbar.new"));
            newBtn.setOnAction(e -> showApplicationForm());
            return new ToolBar(criteriaBtn, newBtn, new Separator(), helpBtn, logoutBtn);
        }
        Button tableBtn = new Button(lang.get("toolbar.table"));
        tableBtn.setOnAction(e -> showApplicationsTable());
        Button dashboardBtn = new Button(lang.get("toolbar.dashboard"));
        dashboardBtn.setOnAction(e -> root.setCenter(createDashboardView()));
        Button databaseBtn = new Button(lang.get("toolbar.database"));
        databaseBtn.setOnAction(e -> showDatabaseWindow((Stage) root.getScene().getWindow()));
        return new ToolBar(dashboardBtn, tableBtn, databaseBtn, new Separator(), helpBtn, logoutBtn);
    }

    private HBox createStatusBar() {
        HBox box = new HBox(statusBar);
        box.getStyleClass().add("status-bar");
        box.setPadding(new Insets(8));
        return box;
    }

    private void configureShortcuts(Scene scene, Stage stage) {
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN), () -> {
            if (currentRole == UserRole.STUDENT) showApplicationForm();
        });
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN), () -> {
            if (currentRole == UserRole.ADMIN) showApplicationsTable();
        });
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN), () -> {
            if (currentRole == UserRole.ADMIN) root.setCenter(createDashboardView());
        });
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.B, KeyCombination.CONTROL_DOWN), () -> {
            if (currentRole == UserRole.ADMIN) showDatabaseWindow(stage);
        });
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F1), this::showHelp);
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.ESCAPE), () -> statusBar.setText(lang.get("status.ready")));
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN), () -> closeApplication(stage));
    }

    private void closeApplication(Stage stage) {
        stage.close();
        Platform.exit();
    }

    private void showLogin(Stage stage) {
        currentRole = null;
        currentUserName = null;
        root.setTop(createLoginMenu(stage));
        root.setBottom(null);
        root.setCenter(createLoginView(stage));
    }

    private MenuBar createLoginMenu(Stage stage) {
        Menu language = new Menu(lang.get("menu.language"));
        MenuItem albanian = new MenuItem("Shqip");
        albanian.setOnAction(e -> {
            lang.setLanguage("sq");
            stage.setTitle(lang.get("app.title"));
            showLogin(stage);
        });
        MenuItem english = new MenuItem("English");
        english.setOnAction(e -> {
            lang.setLanguage("en");
            stage.setTitle(lang.get("app.title"));
            showLogin(stage);
        });
        language.getItems().addAll(albanian, english);
        return new MenuBar(language);
    }

    private BorderPane createLoginView(Stage stage) {
        Label title = new Label(lang.get("login.title"));
        title.getStyleClass().add("heading");
        Label intro = new Label(lang.get("login.description"));
        intro.setWrapText(true);
        TextField username = new TextField();
        username.setPromptText(lang.get("login.username"));
        PasswordField password = new PasswordField();
        password.setPromptText(lang.get("login.password"));
        Button enter = new Button(lang.get("login.enter"));
        enter.setDefaultButton(true);
        Label secure = new Label(lang.get("login.secure"));
        secure.getStyleClass().add("login-security");

        Runnable loginAction = () -> {
            try {
                var account = userRepository.authenticate(username.getText().trim().toLowerCase(), password.getText());
                if (account.isEmpty()) {
                    password.clear();
                    showError(lang.get("msg.error"), lang.get("login.invalid"));
                    return;
                }
                currentRole = UserRole.valueOf(account.get().getRole());
                currentUserName = account.get().getFullName();
                SessionManager.login(account.get().getUsername(), account.get().getRole());
                rebuildLayout(stage);
                statusBar.setText(currentRole == UserRole.STUDENT ? lang.get("status.student") : lang.get("status.admin"));
            } catch (SQLException ex) {
                showError(lang.get("msg.error"), ex.getMessage());
            }
        };
        enter.setOnAction(e -> loginAction.run());
        username.setOnAction(e -> loginAction.run());
        password.setOnAction(e -> loginAction.run());

        Label brand = new Label(lang.get("login.brand"));
        brand.getStyleClass().add("login-brand");
        VBox panel = new VBox(13, brand, title, intro, new Label(lang.get("login.username")), username,
                new Label(lang.get("login.password")), password, new HBox(12, enter, secure));
        panel.setMaxWidth(390);
        panel.setPadding(new Insets(26));
        panel.getStyleClass().add("login-panel");
        HBox shell = new HBox(0, createLoginIllustration(), panel);
        shell.setAlignment(Pos.CENTER);
        shell.getStyleClass().add("login-shell");
        HBox.setHgrow(panel, Priority.ALWAYS);
        StackPane centered = new StackPane(shell);
        centered.setPadding(new Insets(26));
        BorderPane page = new BorderPane(centered);
        page.getStyleClass().add("login-page");
        return page;
    }

    private StackPane createLoginIllustration() {
        Label title = new Label("SCHOLARSHIP");
        title.getStyleClass().add("login-graphic-title");
        Label subtitle = new Label("PORTAL");
        subtitle.getStyleClass().add("login-graphic-subtitle");

        Label university = new Label("U");
        university.getStyleClass().add("login-icon-circle");
        Label target = new Label("*");
        target.getStyleClass().add("login-icon-circle");
        Label money = new Label("$");
        money.getStyleClass().add("login-icon-circle");
        Label book = new Label("B");
        book.getStyleClass().add("login-icon-circle");

        HBox topIcons = new HBox(22, university, target);
        topIcons.setAlignment(Pos.CENTER);
        HBox bottomIcons = new HBox(22, book, money);
        bottomIcons.setAlignment(Pos.CENTER);

        Region lineOne = new Region();
        lineOne.getStyleClass().add("login-graphic-line");
        Region lineTwo = new Region();
        lineTwo.getStyleClass().add("login-graphic-line");

        VBox art = new VBox(12, topIcons, title, subtitle, lineOne, bottomIcons, lineTwo);
        art.setAlignment(Pos.CENTER);
        StackPane visual = new StackPane(art);
        visual.getStyleClass().add("login-visual");
        visual.setMinWidth(520);
        visual.setPrefWidth(620);
        visual.setMaxWidth(680);
        return visual;
    }

    private BorderPane createStudentHomeView() {
        Label badge = new Label(lang.get("login.brand"));
        badge.getStyleClass().add("hero-badge");
        Label welcome = new Label(lang.get("student.title"));
        welcome.getStyleClass().add("hero-title");
        Button apply = new Button(lang.get("student.apply"));
        apply.getStyleClass().add("hero-button");
        apply.setOnAction(e -> showApplicationForm());
        Button criteria = new Button(lang.get("student.criteria"));
        criteria.getStyleClass().add("secondary-button");
        criteria.setOnAction(e -> root.setCenter(createCriteriaView()));

        VBox heroCopy = new VBox(16, badge, welcome, new HBox(12, apply, criteria));
        heroCopy.setAlignment(Pos.CENTER_LEFT);
        HBox hero = new HBox(26, heroCopy, createScholarshipIllustration());
        hero.setAlignment(Pos.CENTER);
        HBox.setHgrow(heroCopy, Priority.ALWAYS);
        hero.getStyleClass().add("student-hero");

        VBox content = new VBox(hero);
        content.setPadding(new Insets(14));
        content.getStyleClass().add("student-home");

        BorderPane page = new BorderPane(content);
        page.setPadding(new Insets(16));
        return page;
    }

    private StackPane createScholarshipIllustration() {
        Label cap = new Label("SCHOLARSHIP");
        cap.getStyleClass().add("illustration-title");
        Label icon = new Label("$");
        icon.getStyleClass().add("illustration-coin");
        Region book = new Region();
        book.getStyleClass().add("illustration-book");
        Region card = new Region();
        card.getStyleClass().add("illustration-card");
        VBox objects = new VBox(12, icon, book);
        objects.setAlignment(Pos.CENTER);
        StackPane illustration = new StackPane(card, objects, cap);
        StackPane.setAlignment(cap, Pos.TOP_LEFT);
        StackPane.setMargin(cap, new Insets(24, 0, 0, 26));
        illustration.getStyleClass().add("scholarship-illustration");
        illustration.setMinSize(330, 220);
        illustration.setPrefSize(390, 250);
        return illustration;
    }

    private BorderPane createCriteriaView() {
        Label title = new Label(lang.get("criteria.title"));
        title.getStyleClass().add("criteria-title");
        Label lead = new Label(lang.get("criteria.description"));
        lead.setWrapText(true);
        lead.getStyleClass().add("criteria-lead");
        VBox academic = criteriaSection(lang.get("criteria.academicTitle"), lang.get("criteria.academic"));
        VBox financial = criteriaSection(lang.get("criteria.financialTitle"), lang.get("criteria.financial"));
        VBox documents = criteriaSection(lang.get("criteria.documentsTitle"), lang.get("criteria.documents"));
        VBox review = criteriaSection(lang.get("criteria.reviewTitle"), lang.get("criteria.review"));
        Button apply = new Button(lang.get("student.apply"));
        apply.setOnAction(e -> showApplicationForm());
        TilePane grid = new TilePane(16, 16, academic, financial, documents, review);
        grid.setPrefColumns(2);
        grid.setMaxWidth(Double.MAX_VALUE);
        VBox content = new VBox(18, title, lead, grid, apply);
        content.setPadding(new Insets(32));
        content.getStyleClass().add("criteria-page");
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("page-scroll");
        return createPage(lang.get("page.criteria"), scroll);
    }

    private VBox criteriaSection(String title, String text) {
        Label heading = new Label(title);
        heading.getStyleClass().add("criteria-section-title");
        Label body = new Label(text);
        body.setWrapText(true);
        VBox section = new VBox(10, heading, body);
        section.setPrefWidth(420);
        section.getStyleClass().add("criteria-section");
        return section;
    }

    private void logout(Stage stage) {
        table = null;
        statusBar.setText(lang.get("status.ready"));
        SessionManager.logout();
        showLogin(stage);
    }

    private void showApplicationForm() {
    }

    private void showApplicationsTable() {
    }

    private void showApplicationsTable(String initialFilter) {
    }

    private BorderPane createDashboardView() {
        return null;
    }

    private VBox priorityQueue() throws SQLException {
        return null;
    }

    private Label metric(String title, String value) {
        return null;
    }

    private Label metric(String title, String value, Runnable action) {
        return null;
    }

    private PieChart.Data statusSlice(String status, int count) {
        return null;
    }

    private Map<String, List<String>> studyPrograms() {
        return null;
    }

    private BorderPane createPage(String title, javafx.scene.Node content) {
        return null;
    }

    private void loadTable(String keyword) {
    }

    private void changeSelectedStatus(String status) {
    }

    private void deleteSelected() {
    }

    private void showSelectedDetails() {
    }

    private void showHelp() {
    }

    private void showDatabaseWindow(Stage owner) {
    }

    private void validate(TextField name, TextField index, TextField email, TextField phone, ComboBox<String> municipality,
                          ComboBox<String> faculty, ComboBox<String> program, TextField average, TextField income,
                          ComboBox<String> category, ComboBox<String> type, ComboBox<String> cycle, CheckBox activeStudent,
                          CheckBox noOtherScholarship, CheckBox identityDoc, CheckBox transcriptDoc, CheckBox incomeDoc,
                          CheckBox studentDoc, CheckBox documents) {
    }

    private String documentSummary(CheckBox identityDoc, CheckBox transcriptDoc, CheckBox incomeDoc, CheckBox studentDoc) {
        return null;
    }

    private boolean requireText(TextInputControl field) {
        return false;
    }

    private boolean requireCombo(ComboBox<?> comboBox) {
        return false;
    }

    private double parseDoubleOrMark(TextField field, String message) {
        return 0;
    }

    private void showFieldError(javafx.scene.Node field, String message) {
    }

    private void clearFieldErrors() {
    }

    private void allowDecimalInput(TextField field) {
    }

    private void keepPhonePrefix(TextField phone) {
    }

    private void addRow(GridPane grid, int row, String label, javafx.scene.Node node) {
    }

    private void addRequiredRow(GridPane grid, int row, String label, javafx.scene.Node node) {
    }

    private void addFormRow(GridPane grid, int row, String label, javafx.scene.Node node, boolean required) {
    }

    private TitledPane group(String title, Region content) {
        return null;
    }

    private void setTabOrder(List<javafx.scene.Node> nodes) {
    }

    private void showInfo(String title, String message) {
    }

    private void showError(String title, String message) {
    }
}

