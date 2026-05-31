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
        fieldErrors = new LinkedHashMap<>();
        TextField name = new TextField();
        TextField index = new TextField();
        TextField email = new TextField();
        TextField phone = new TextField();
        phone.setText("+383");
        phone.setPromptText("+383 XX XXX XXX");
        keepPhonePrefix(phone);
        ComboBox<String> municipality = new ComboBox<>(FXCollections.observableArrayList(
                "Prishtine", "Prizren", "Peje", "Gjakove", "Gjilan", "Ferizaj", "Mitrovice", "Podujeve", "Vushtrri", "Suhareke", "Rahovec", "Other"));
        ComboBox<String> faculty = new ComboBox<>(FXCollections.observableArrayList(studyPrograms().keySet()));
        ComboBox<String> program = new ComboBox<>();
        faculty.setMaxWidth(Double.MAX_VALUE);
        program.setMaxWidth(Double.MAX_VALUE);
        program.setDisable(true);
        faculty.setPromptText(lang.get("form.chooseFaculty"));
        program.setPromptText(lang.get("form.chooseProgram"));
        faculty.setOnAction(e -> {
            program.setItems(FXCollections.observableArrayList(studyPrograms().getOrDefault(faculty.getValue(), List.of())));
            program.setValue(null);
            program.setDisable(program.getItems().isEmpty());
        });
        Spinner<Integer> year = new Spinner<>(1, 5, 1);
        Spinner<Integer> ects = new Spinner<>(0, 300, 60, 6);
        TextField average = new TextField();
        average.setPromptText("8.0 - 10.0");
        allowDecimalInput(average);
        TextField income = new TextField();
        income.setPromptText("0.00");
        allowDecimalInput(income);
        Spinner<Integer> household = new Spinner<>(1, 15, 4);
        ComboBox<String> category = new ComboBox<>(FXCollections.observableArrayList(
                "General", "Low-income household", "Orphan support", "Disability support", "First-generation student", "Minority community", "Single-parent household"));
        ComboBox<String> cycle = new ComboBox<>(FXCollections.observableArrayList(
                "Annual 2026/27", "Semester Fall 2026", "Semester Spring 2027", "Emergency support", "Mobility support"));
        ComboBox<String> type = new ComboBox<>(FXCollections.observableArrayList(
                "Excellence and Need", "Merit Scholarship", "Social Support", "STEM Scholarship", "Research Project",
                "Sports Achievement", "Arts and Culture", "Women in STEM", "Mobility Grant", "Final-year Completion"));
        ToggleGroup genderGroup = new ToggleGroup();
        RadioButton male = new RadioButton(lang.get("form.male"));
        RadioButton female = new RadioButton(lang.get("form.female"));
        male.setToggleGroup(genderGroup); female.setToggleGroup(genderGroup); male.setSelected(true);
        CheckBox activeStudent = new CheckBox(lang.get("form.activeStudent"));
        CheckBox noOtherScholarship = new CheckBox(lang.get("form.noOtherScholarship"));
        CheckBox documents = new CheckBox(lang.get("form.documents"));
        CheckBox identityDoc = new CheckBox(lang.get("form.identityDoc"));
        CheckBox transcriptDoc = new CheckBox(lang.get("form.transcriptDoc"));
        CheckBox incomeDoc = new CheckBox(lang.get("form.incomeDoc"));
        CheckBox studentDoc = new CheckBox(lang.get("form.studentDoc"));
        TextArea note = new TextArea();
        note.setPromptText(lang.get("form.notePrompt"));
        note.setPrefRowCount(3);

        Button save = new Button(lang.get("button.save"));
        save.setDefaultButton(true);
        Button clear = new Button(lang.get("button.clear"));

        GridPane studentFields = new GridPane();
        studentFields.setHgap(10);
        studentFields.setVgap(8);
        addRequiredRow(studentFields, 0, lang.get("form.name"), name);
        addRequiredRow(studentFields, 1, lang.get("form.index"), index);
        addRequiredRow(studentFields, 2, lang.get("form.email"), email);
        addRequiredRow(studentFields, 3, lang.get("form.phone"), phone);
        addRequiredRow(studentFields, 4, lang.get("form.municipality"), municipality);
        addRequiredRow(studentFields, 5, lang.get("form.faculty"), faculty);
        addRequiredRow(studentFields, 6, lang.get("form.program"), program);
        addRow(studentFields, 7, lang.get("form.year"), year);
        addRow(studentFields, 8, lang.get("form.ects"), ects);
        addRow(studentFields, 9, lang.get("form.gender"), new HBox(15, male, female));

        GridPane scholarshipFields = new GridPane();
        scholarshipFields.setHgap(10);
        scholarshipFields.setVgap(8);
        addRequiredRow(scholarshipFields, 0, lang.get("form.average"), average);
        addRequiredRow(scholarshipFields, 1, lang.get("form.income"), income);
        addRow(scholarshipFields, 2, lang.get("form.household"), household);
        addRequiredRow(scholarshipFields, 3, lang.get("form.category"), category);
        addRequiredRow(scholarshipFields, 4, lang.get("form.type"), type);
        addRequiredRow(scholarshipFields, 5, lang.get("form.cycle"), cycle);
        addRequiredRow(scholarshipFields, 6, lang.get("form.eligibility"), new VBox(8, activeStudent, noOtherScholarship));
        addRequiredRow(scholarshipFields, 7, lang.get("form.checklist"), new VBox(8, identityDoc, transcriptDoc, incomeDoc, studentDoc, documents));
        addRow(scholarshipFields, 8, lang.get("form.note"), note);
        scholarshipFields.add(new HBox(12, save, clear), 1, 9);

        VBox grid = new VBox(10,
                group(lang.get("form.studentGroup"), studentFields),
                group(lang.get("form.scholarshipGroup"), scholarshipFields));
        grid.setPadding(new Insets(14));
        grid.getStyleClass().add("card");

        setTabOrder(List.of(name, index, email, phone, municipality, faculty, program, year.getEditor(), ects.getEditor(),
                male, female, average, income, household.getEditor(), category, type, cycle, activeStudent,
                noOtherScholarship, identityDoc, transcriptDoc, incomeDoc, studentDoc, documents, note, save, clear));

        save.setOnAction(e -> {
            try {
                clearFieldErrors();
                validate(name, index, email, phone, municipality, faculty, program, average, income, category, type, cycle,
                        activeStudent, noOtherScholarship, identityDoc, transcriptDoc, incomeDoc, studentDoc, documents);
                if (repository.existsByIndexNumber(index.getText().trim())) {
                    showFieldError(index, lang.get("validation.duplicateIndex"));
                    index.requestFocus();
                    return;
                }
                double avg = Double.parseDouble(average.getText().trim());
                double inc = Double.parseDouble(income.getText().trim());
                double score = scoringService.calculateScore(avg, inc, year.getValue());
                ScholarshipApplication app = new ScholarshipApplication();
                app.setStudentName(name.getText().trim());
                app.setIndexNumber(index.getText().trim());
                app.setFaculty(faculty.getValue());
                app.setStudyProgram(program.getValue());
                app.setStudyYear(year.getValue());
                app.setAverageGrade(avg);
                app.setFamilyIncome(inc);
                app.setScholarshipType(type.getValue());
                app.setEmail(email.getText().trim());
                app.setPhone(phone.getText().trim());
                app.setMunicipality(municipality.getValue());
                app.setEctsCredits(ects.getValue());
                app.setHouseholdMembers(household.getValue());
                app.setSpecialCategory(category.getValue());
                app.setScholarshipCycle(cycle.getValue());
                app.setMotivation(note.getText().trim());
                app.setDocumentSummary(documentSummary(identityDoc, transcriptDoc, incomeDoc, studentDoc));
                app.setStatus("Pending");
                app.setGender(male.isSelected() ? "Male" : "Female");
                app.setDocumentsConfirmed(documents.isSelected());
                app.setAiScore(score);
                app.setAiRecommendation(scoringService.recommendation(score, inc));
                repository.save(app);
                showInfo(lang.get("msg.saved"), lang.get("msg.savedDetails") + "\n"
                        + lang.get("msg.priority") + " " + score + " - " + app.getAiRecommendation());
                statusBar.setText(lang.get("status.saved"));
                if (currentRole == UserRole.STUDENT) {
                    root.setCenter(createStudentHomeView());
                } else {
                    showApplicationsTable();
                }
            } catch (Exception ex) {
                statusBar.setText(ex.getMessage() == null ? lang.get("msg.error") : ex.getMessage());
            }
        });

        clear.setOnAction(e -> {
            clearFieldErrors();
            name.clear(); index.clear(); email.clear(); phone.clear(); municipality.setValue(null); faculty.setValue(null);
            phone.setText("+383");
            program.setItems(FXCollections.observableArrayList()); program.setValue(null); program.setDisable(true);
            average.clear(); income.clear(); type.setValue(null); category.setValue(null); cycle.setValue(null);
            activeStudent.setSelected(false); noOtherScholarship.setSelected(false); documents.setSelected(false);
            identityDoc.setSelected(false); transcriptDoc.setSelected(false); incomeDoc.setSelected(false); studentDoc.setSelected(false); note.clear();
        });

        ScrollPane formScroll = new ScrollPane(grid);
        formScroll.setFitToWidth(true);
        formScroll.setPannable(true);
        formScroll.getStyleClass().add("page-scroll");
        BorderPane page = createPage(lang.get("page.apply"), formScroll);
        root.setCenter(page);
        statusBar.setText(lang.get("status.apply"));
    }

    private void showApplicationsTable() {
        showApplicationsTable("");
    }

    private void showApplicationsTable(String initialFilter) {
        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        TableColumn<ScholarshipApplication, Number> id = new TableColumn<>("ID");
        id.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()));
        TableColumn<ScholarshipApplication, String> name = new TableColumn<>(lang.get("table.name"));
        name.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStudentName()));
        TableColumn<ScholarshipApplication, String> index = new TableColumn<>(lang.get("table.index"));
        index.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getIndexNumber()));
        TableColumn<ScholarshipApplication, String> faculty = new TableColumn<>(lang.get("table.faculty"));
        faculty.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFaculty()));
        TableColumn<ScholarshipApplication, String> type = new TableColumn<>(lang.get("table.type"));
        type.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getScholarshipType()));
        TableColumn<ScholarshipApplication, String> program = new TableColumn<>(lang.get("table.program"));
        program.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStudyProgram()));
        TableColumn<ScholarshipApplication, String> cycle = new TableColumn<>(lang.get("table.cycle"));
        cycle.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getScholarshipCycle()));
        TableColumn<ScholarshipApplication, Number> year = new TableColumn<>(lang.get("table.year"));
        year.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getStudyYear()));
        TableColumn<ScholarshipApplication, Number> average = new TableColumn<>(lang.get("table.average"));
        average.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getAverageGrade()));
        TableColumn<ScholarshipApplication, Number> income = new TableColumn<>(lang.get("table.income"));
        income.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getFamilyIncome()));
        TableColumn<ScholarshipApplication, String> status = new TableColumn<>(lang.get("table.status"));
        status.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));
        TableColumn<ScholarshipApplication, Number> score = new TableColumn<>("AI Score");
        score.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getAiScore()));
        TableColumn<ScholarshipApplication, String> rec = new TableColumn<>(lang.get("table.recommendation"));
        rec.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAiRecommendation()));
        table.getColumns().addAll(id, name, index, faculty, program, type, cycle, year, average, income, status, score, rec);

        TextField search = new TextField();
        search.setPromptText(lang.get("table.search"));
        search.setText(initialFilter);
        search.setOnAction(e -> loadTable(search.getText()));
        Button searchBtn = new Button(lang.get("button.search"));
        Button refresh = new Button(lang.get("button.refresh"));
        Button approve = new Button(lang.get("button.approve"));
        Button reject = new Button(lang.get("button.reject"));
        Button details = new Button(lang.get("button.details"));
        Button delete = new Button(lang.get("button.delete"));
        HBox actions = new HBox(10, search, searchBtn, refresh, details, approve, reject, delete);
        actions.setAlignment(Pos.CENTER_LEFT);

        searchBtn.setOnAction(e -> loadTable(search.getText()));
        refresh.setOnAction(e -> loadTable(""));
        approve.setOnAction(e -> changeSelectedStatus("Approved"));
        reject.setOnAction(e -> changeSelectedStatus("Rejected"));
        details.setOnAction(e -> showSelectedDetails());
        delete.setOnAction(e -> deleteSelected());

        ContextMenu contextMenu = new ContextMenu();
        MenuItem approveItem = new MenuItem(lang.get("button.approve"));
        approveItem.setOnAction(e -> changeSelectedStatus("Approved"));
        MenuItem rejectItem = new MenuItem(lang.get("button.reject"));
        rejectItem.setOnAction(e -> changeSelectedStatus("Rejected"));
        MenuItem deleteItem = new MenuItem(lang.get("button.delete"));
        deleteItem.setOnAction(e -> deleteSelected());
        MenuItem detailsItem = new MenuItem(lang.get("button.details"));
        detailsItem.setOnAction(e -> showSelectedDetails());
        contextMenu.getItems().addAll(detailsItem, new SeparatorMenuItem(), approveItem, rejectItem, new SeparatorMenuItem(), deleteItem);
        table.setContextMenu(contextMenu);

        VBox content = new VBox(12, actions, table);
        content.setPadding(new Insets(20));
        content.getStyleClass().add("card");
        VBox.setVgrow(table, Priority.ALWAYS);
        root.setCenter(createPage(lang.get("page.applications"), content));
        setTabOrder(List.of(search, searchBtn, refresh, details, approve, reject, delete, table));
        loadTable(initialFilter);
        statusBar.setText(lang.get("status.table"));
    }

    private BorderPane createDashboardView() {
        VBox wrapper = new VBox(18);
        wrapper.setPadding(new Insets(20));
        try {
            Label adminHeading = new Label(lang.get("admin.title") + " - " + currentUserName);
            adminHeading.getStyleClass().add("portal-banner");
            Label total = metric(lang.get("dash.total"), String.valueOf(repository.countAll()), () -> showApplicationsTable(""));
            Label pending = metric(lang.get("dash.pending"), String.valueOf(repository.countByStatus("Pending")), () -> showApplicationsTable("Pending"));
            Label approved = metric(lang.get("dash.approved"), String.valueOf(repository.countByStatus("Approved")), () -> showApplicationsTable("Approved"));
            Label rejected = metric(lang.get("dash.rejected"), String.valueOf(repository.countByStatus("Rejected")), () -> showApplicationsTable("Rejected"));
            Label avgScore = metric(lang.get("dash.avgScore"), String.valueOf(repository.averageScore()));
            HBox metrics = new HBox(14, total, pending, approved, rejected, avgScore);
            metrics.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));

            PieChart.Data pendingSlice = statusSlice("Pending", repository.countByStatus("Pending"));
            PieChart.Data approvedSlice = statusSlice("Approved", repository.countByStatus("Approved"));
            PieChart.Data rejectedSlice = statusSlice("Rejected", repository.countByStatus("Rejected"));
            PieChart pieChart = new PieChart(FXCollections.observableArrayList(pendingSlice, approvedSlice, rejectedSlice));
            pieChart.setTitle(lang.get("dash.statusChart"));

            CategoryAxis xAxis = new CategoryAxis();
            NumberAxis yAxis = new NumberAxis();
            BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
            barChart.setTitle(lang.get("dash.facultyChart"));
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(lang.get("dash.applications"));
            for (String[] row : repository.countByFaculty()) series.getData().add(new XYChart.Data<>(row[0], Integer.parseInt(row[1])));
            barChart.getData().add(series);

            Label biAi = new Label(lang.get("dash.biAiText"));
            biAi.getStyleClass().add("ai-box");
            VBox queue = priorityQueue();
            HBox charts = new HBox(18, pieChart, barChart);
            HBox.setHgrow(barChart, Priority.ALWAYS);
            wrapper.getChildren().addAll(adminHeading, metrics, charts, queue, biAi);
        } catch (SQLException e) {
            wrapper.getChildren().add(new Label(e.getMessage()));
        }
        ScrollPane dashboardScroll = new ScrollPane(wrapper);
        dashboardScroll.setFitToWidth(true);
        dashboardScroll.getStyleClass().add("page-scroll");
        return createPage(lang.get("page.dashboard"), dashboardScroll);
    }

    private VBox priorityQueue() throws SQLException {
        TableView<ScholarshipApplication> queue = new TableView<>();
        queue.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        queue.setPrefHeight(210);
        TableColumn<ScholarshipApplication, String> student = new TableColumn<>(lang.get("table.name"));
        student.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStudentName()));
        TableColumn<ScholarshipApplication, String> scholarship = new TableColumn<>(lang.get("table.type"));
        scholarship.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getScholarshipType()));
        TableColumn<ScholarshipApplication, Number> income = new TableColumn<>(lang.get("table.income"));
        income.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getFamilyIncome()));
        TableColumn<ScholarshipApplication, Number> score = new TableColumn<>("Priority");
        score.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getAiScore()));
        queue.getColumns().addAll(student, scholarship, income, score);
        queue.setItems(FXCollections.observableArrayList(repository.topPending(5)));
        queue.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) showApplicationsTable("Pending");
        });

        Button review = new Button(lang.get("dash.reviewQueue"));
        review.setOnAction(e -> showApplicationsTable("Pending"));
        Label title = new Label(lang.get("dash.queueTitle"));
        title.getStyleClass().add("subheading");
        VBox panel = new VBox(10, new HBox(12, title, review), queue);
        panel.setPadding(new Insets(16));
        panel.getStyleClass().add("work-panel");
        return panel;
    }

    private Label metric(String title, String value) {
        return metric(title, value, null);
    }

    private Label metric(String title, String value, Runnable action) {
        Label label = new Label(title + "\n" + value);
        label.getStyleClass().add("metric-card");
        label.setMaxWidth(Double.MAX_VALUE);
        if (action != null) {
            label.getStyleClass().add("clickable-metric");
            label.setFocusTraversable(true);
            label.setOnMouseClicked(e -> action.run());
            label.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.SPACE) {
                    action.run();
                    e.consume();
                }
            });
        }
        return label;
    }

    private PieChart.Data statusSlice(String status, int count) {
        PieChart.Data data = new PieChart.Data(status, count);
        data.nodeProperty().addListener((ignored, previous, node) -> {
            if (node != null) {
                node.getStyleClass().add("clickable-chart-slice");
                node.setOnMouseClicked(e -> showApplicationsTable(status));
            }
        });
        return data;
    }

    private Map<String, List<String>> studyPrograms() {
        Map<String, List<String>> programs = new LinkedHashMap<>();
        programs.put("UP - Fakulteti Filozofik", List.of("Filozofi", "Sociologji", "Psikologji", "Histori", "Shkenca Politike"));
        programs.put("UP - Fakulteti i Shkencave Matematike-Natyrore", List.of("Matematike", "Shkenca Kompjuterike", "Fizike", "Kimi", "Biologji", "Gjeografi"));
        programs.put("UP - Fakulteti i Filologjise", List.of("Gjuhe dhe Letersi Shqipe", "Gjuhe Angleze", "Gjuhe Gjermane", "Gjuhe Frënge", "Gazetari"));
        programs.put("UP - Fakulteti Juridik", List.of("Juridik i Pergjithshem"));
        programs.put("UP - Fakulteti Ekonomik", List.of("Banka dhe Financa", "Menaxhment", "Marketing", "Kontabilitet", "Ekonomi e Aplikuar"));
        programs.put("UP - Fakulteti i Inxhinierise se Ndertimit", List.of("Ndertimtari", "Hidroteknike", "Gjeodezi"));
        programs.put("UP - Fakulteti i Inxhinierise Elektrike dhe Kompjuterike", List.of(
                "Inxhinieri Kompjuterike dhe Softuerike",
                "Elektronike, Automatike dhe Robotike",
                "Teknologji e Informacionit dhe Komunikimit",
                "Elektroenergjetike"));
        programs.put("UP - Fakulteti i Inxhinierise Mekanike", List.of("Konstruksione dhe Mekanizim", "Termoenergjetike", "Komunikacion", "Mekatronike"));
        programs.put("UP - Fakulteti i Mjekesise", List.of("Mjekesi e Pergjithshme", "Stomatologji", "Farmaci", "Fizioterapi", "Infermieristikë"));
        programs.put("UP - Fakulteti i Arteve", List.of("Art Figurativ", "Art Muzikor", "Art Dramatik"));
        programs.put("UP - Fakulteti i Bujqesise dhe Veterinarise", List.of("Bujqesi", "Veterinari", "Teknologji Ushqimore", "Ekonomi e Bujqesise"));
        programs.put("UP - Fakulteti i Shkencave Sportive", List.of("Edukim Fizik dhe Sport", "Trajner Sportiv"));
        programs.put("UP - Fakulteti i Edukimit", List.of("Edukim Fillor", "Edukim Parashkollor", "Pedagogji"));
        programs.put("UP - Fakulteti i Arkitektures", List.of("Arkitekture"));

        programs.put("UPZ - Fakulteti i Shkencave Kompjuterike", List.of("Shkenca Kompjuterike", "Teknologji Informacioni dhe Telekomunikim"));
        programs.put("UPZ - Fakulteti Ekonomik", List.of("Administrim Biznesi", "Menaxhment Nderkombetar"));
        programs.put("UPZ - Fakulteti Juridik", List.of("Juridik"));
        programs.put("UPZ - Fakulteti i Edukimit", List.of("Edukim Fillor", "Edukim Parashkollor"));
        programs.put("UPZ - Fakulteti i Filologjise", List.of("Gjuhe Shqipe", "Gjuhe Angleze", "Gjuhe Gjermane"));
        programs.put("UPZ - Fakulteti i Shkencave te Jetes dhe Mjedisit", List.of("Agrobiznes", "Shkenca Pyjore dhe Mjedisore"));

        programs.put("UKZ - Fakulteti i Shkencave Kompjuterike", List.of("Shkenca Kompjuterike", "Inxhinieri Softuerike"));
        programs.put("UKZ - Fakulteti Ekonomik", List.of("Banka, Financa dhe Kontabilitet", "Menaxhment"));
        programs.put("UKZ - Fakulteti Juridik", List.of("Juridik"));
        programs.put("UKZ - Fakulteti i Edukimit", List.of("Edukim Fillor", "Edukim Parashkollor"));
        programs.put("UKZ - Fakulteti i Shkencave Aplikative", List.of("Inxhinieri Industriale", "Menaxhim i Resurseve"));

        programs.put("UHZ - Fakulteti i Biznesit", List.of("Administrim Biznesi", "Banka dhe Financa", "Kontabilitet"));
        programs.put("UHZ - Fakulteti Juridik", List.of("Juridik"));
        programs.put("UHZ - Fakulteti i Menaxhimit ne Turizem, Hoteleri dhe Mjedis", List.of("Turizem dhe Hoteleri", "Menaxhim Mjedisor"));
        programs.put("UHZ - Fakulteti i Agrobiznesit", List.of("Agrobiznes", "Teknologji Ushqimore"));

        programs.put("UMIB - Fakulteti i Gjeoshkencave", List.of("Gjeologji", "Miniera", "Materiale dhe Metalurgji"));
        programs.put("UMIB - Fakulteti i Inxhinierise Mekanike dhe Kompjuterike", List.of("Inxhinieri Mekanike", "Inxhinieri Kompjuterike"));
        programs.put("UMIB - Fakulteti Ekonomik", List.of("Menaxhment", "Financa"));
        programs.put("UASF - Fakulteti i Inxhinierise dhe Informatikes", List.of("Informatike e Aplikuar", "Inxhinieri Industriale", "Dizajn Grafik dhe Multimedia"));
        return programs;
    }

    private BorderPane createPage(String title, javafx.scene.Node content) {
        Label heading = new Label(title);
        heading.getStyleClass().add("heading");
        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(14));
        pane.getStyleClass().add("soft-page");
        pane.setTop(heading);
        BorderPane.setMargin(heading, new Insets(0, 0, 12, 0));
        pane.setCenter(content);
        return pane;
    }

    private void loadTable(String keyword) {
        try {
            table.setItems(FXCollections.observableArrayList(repository.search(keyword == null ? "" : keyword)));
        } catch (SQLException e) {
            showError(lang.get("msg.error"), e.getMessage());
        }
    }

    private void changeSelectedStatus(String status) {
        ScholarshipApplication selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) { showError(lang.get("msg.error"), lang.get("msg.selectRow")); return; }
        try { repository.updateStatus(selected.getId(), status); loadTable(""); } catch (SQLException e) { showError(lang.get("msg.error"), e.getMessage()); }
    }

    private void deleteSelected() {
        ScholarshipApplication selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) { showError(lang.get("msg.error"), lang.get("msg.selectRow")); return; }
        try { repository.delete(selected.getId()); loadTable(""); } catch (SQLException e) { showError(lang.get("msg.error"), e.getMessage()); }
    }

    private void showSelectedDetails() {
        ScholarshipApplication selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError(lang.get("msg.error"), lang.get("msg.selectRow"));
            return;
        }
        TextArea details = new TextArea(String.format("""
                %s: %s
                %s: %s
                %s: %s
                %s: %s
                %s: %s
                %s: %d
                %s: %.2f
                %s: %.2f
                %s: %d
                %s: %s
                %s: %s
                %s: %s
                %s: %s

                %s:
                %s
                """,
                lang.get("table.name"), selected.getStudentName(),
                lang.get("table.index"), selected.getIndexNumber(),
                lang.get("form.email"), selected.getEmail(),
                lang.get("form.phone"), selected.getPhone(),
                lang.get("form.municipality"), selected.getMunicipality(),
                lang.get("form.ects"), selected.getEctsCredits(),
                lang.get("table.average"), selected.getAverageGrade(),
                lang.get("table.income"), selected.getFamilyIncome(),
                lang.get("form.household"), selected.getHouseholdMembers(),
                lang.get("form.category"), selected.getSpecialCategory(),
                lang.get("form.cycle"), selected.getScholarshipCycle(),
                lang.get("form.checklist"), selected.getDocumentSummary(),
                lang.get("table.recommendation"), selected.getAiRecommendation(),
                lang.get("form.note"), selected.getMotivation()));
        details.setEditable(false);
        details.setWrapText(true);
        details.setPrefSize(640, 460);
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle(lang.get("button.details"));
        dialog.setHeaderText(selected.getStudentName() + " - " + selected.getScholarshipType());
        dialog.getDialogPane().setContent(details);
        dialog.showAndWait();
    }

    private void showHelp() {
        String content = currentRole == UserRole.STUDENT ? lang.get("help.studentContent") : lang.get("help.content");
        TextArea help = new TextArea(content);
        help.setWrapText(true);
        help.setEditable(false);
        help.setPrefSize(720, 420);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(lang.get("menu.help"));
        alert.setHeaderText(lang.get("help.title"));
        alert.getDialogPane().setContent(help);
        alert.showAndWait();
    }

    private void showDatabaseWindow(Stage owner) {
        TextField url = new TextField(Database.url());
        url.setEditable(false);

        Label state = new Label(lang.get("db.notTested"));
        Button test = new Button(lang.get("db.test"));
        Button openTable = new Button(lang.get("db.openTable"));
        Button close = new Button(lang.get("button.close"));

        GridPane details = new GridPane();
        details.setHgap(12);
        details.setVgap(12);
        addRow(details, 0, lang.get("db.driver"), new Label("SQLite JDBC"));
        addRow(details, 1, lang.get("db.url"), url);
        addRow(details, 2, lang.get("db.status"), state);

        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle(lang.get("db.title"));

        test.setDefaultButton(true);
        test.setOnAction(e -> {
            try (Connection ignored = Database.getConnection()) {
                state.setText(lang.get("db.connected"));
                statusBar.setText(lang.get("status.databaseConnected"));
            } catch (SQLException ex) {
                state.setText(lang.get("db.failed"));
                showError(lang.get("msg.error"), ex.getMessage());
            }
        });
        openTable.setOnAction(e -> {
            dialog.close();
            showApplicationsTable();
        });
        close.setOnAction(e -> dialog.close());

        HBox actions = new HBox(10, test, openTable, close);
        VBox content = new VBox(14, details, actions);
        content.setPadding(new Insets(20));
        content.getStyleClass().add("card");

        Scene scene = new Scene(content, 560, 280);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        dialog.setScene(scene);
        setTabOrder(List.of(url, test, openTable, close));
        dialog.showAndWait();
    }

    private void validate(TextField name, TextField index, TextField email, TextField phone, ComboBox<String> municipality,
                          ComboBox<String> faculty, ComboBox<String> program, TextField average, TextField income,
                          ComboBox<String> category, ComboBox<String> type, ComboBox<String> cycle, CheckBox activeStudent,
                          CheckBox noOtherScholarship, CheckBox identityDoc, CheckBox transcriptDoc, CheckBox incomeDoc,
                          CheckBox studentDoc, CheckBox documents) {
        boolean valid = true;
        valid &= requireText(name);
        valid &= requireText(index);
        valid &= requireText(email);
        valid &= requireText(phone);
        valid &= requireCombo(municipality);
        valid &= requireCombo(faculty);
        valid &= requireCombo(program);
        valid &= requireText(average);
        valid &= requireText(income);
        valid &= requireCombo(category);
        valid &= requireCombo(type);
        valid &= requireCombo(cycle);

        if (!name.getText().isBlank() && !name.getText().trim().contains(" ")) {
            showFieldError(name, lang.get("validation.fullName"));
            valid = false;
        }
        if (!index.getText().isBlank() && !index.getText().trim().matches("[A-Za-z0-9/-]{4,20}")) {
            showFieldError(index, lang.get("validation.index"));
            valid = false;
        }
        if (!email.getText().isBlank() && !email.getText().trim().matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            showFieldError(email, lang.get("validation.email"));
            valid = false;
        }
        if (!phone.getText().trim().matches("\\+383[0-9 ]{6,15}")) {
            showFieldError(phone, lang.get("validation.phone"));
            valid = false;
        }
        double avg = parseDoubleOrMark(average, lang.get("validation.number"));
        double inc = parseDoubleOrMark(income, lang.get("validation.number"));
        if (!average.getText().isBlank() && (avg < ScoringService.MINIMUM_AVERAGE || avg > 10)) {
            showFieldError(average, lang.get("validation.average"));
            valid = false;
        }
        if (!income.getText().isBlank() && inc < 0) {
            showFieldError(income, lang.get("validation.income"));
            valid = false;
        }
        if (!activeStudent.isSelected() || !noOtherScholarship.isSelected()) {
            showFieldError(activeStudent.getParent(), lang.get("validation.activeStudent") + " " + lang.get("validation.noOtherScholarship"));
            valid = false;
        }
        if (!identityDoc.isSelected() || !transcriptDoc.isSelected() || !incomeDoc.isSelected() || !studentDoc.isSelected()) {
            showFieldError(identityDoc.getParent(), lang.get("validation.checklist"));
            valid = false;
        }
        if (!documents.isSelected()) {
            showFieldError(identityDoc.getParent(), lang.get("validation.documents"));
            valid = false;
        }
        if (!valid) throw new IllegalArgumentException(lang.get("validation.inlineSummary"));
    }

    private String documentSummary(CheckBox identityDoc, CheckBox transcriptDoc, CheckBox incomeDoc, CheckBox studentDoc) {
        return String.join(", ",
                identityDoc.getText(),
                transcriptDoc.getText(),
                incomeDoc.getText(),
                studentDoc.getText());
    }

    private boolean requireText(TextInputControl field) {
        if (field.getText().isBlank()) {
            showFieldError(field, lang.get("validation.requiredField"));
            return false;
        }
        return true;
    }

    private boolean requireCombo(ComboBox<?> comboBox) {
        if (comboBox.getValue() == null) {
            showFieldError(comboBox, lang.get("validation.requiredField"));
            return false;
        }
        return true;
    }

    private double parseDoubleOrMark(TextField field, String message) {
        if (field.getText().isBlank()) return -1;
        try {
            return Double.parseDouble(field.getText().trim());
        } catch (NumberFormatException ex) {
            showFieldError(field, message);
            return -1;
        }
    }

    private void showFieldError(javafx.scene.Node field, String message) {
        Label error = fieldErrors.get(field);
        if (error == null && field.getParent() != null) {
            error = fieldErrors.get(field.getParent());
        }
        if (error != null) {
            error.setText(message);
            error.setManaged(true);
            error.setVisible(true);
        }
        field.getStyleClass().remove("input-error");
        field.getStyleClass().add("input-error");
    }

    private void clearFieldErrors() {
        for (Map.Entry<javafx.scene.Node, Label> entry : fieldErrors.entrySet()) {
            entry.getValue().setText("");
            entry.getValue().setManaged(false);
            entry.getValue().setVisible(false);
            entry.getKey().getStyleClass().remove("input-error");
        }
    }

    private void allowDecimalInput(TextField field) {
        field.textProperty().addListener((ignored, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                field.setText(oldValue);
            }
        });
    }

    private void keepPhonePrefix(TextField phone) {
        phone.textProperty().addListener((ignored, oldValue, newValue) -> {
            String value = newValue == null ? "" : newValue;
            if (!value.startsWith("+383")) {
                phone.setText("+383");
                phone.positionCaret(phone.getText().length());
                return;
            }
            if (!value.matches("\\+383[0-9 ]*")) {
                phone.setText(oldValue);
            }
        });
    }

    private void addRow(GridPane grid, int row, String label, javafx.scene.Node node) {
        addFormRow(grid, row, label, node, false);
    }

    private void addRequiredRow(GridPane grid, int row, String label, javafx.scene.Node node) {
        addFormRow(grid, row, label, node, true);
    }

    private void addFormRow(GridPane grid, int row, String label, javafx.scene.Node node, boolean required) {
        Label lbl = new Label(label);
        if (required) lbl.setText(label + " *");
        lbl.setMinWidth(135);
        Label error = new Label();
        error.getStyleClass().add("field-error");
        error.setManaged(false);
        error.setVisible(false);
        VBox fieldBox = new VBox(4, node, error);
        grid.add(lbl, 0, row);
        grid.add(fieldBox, 1, row);
        fieldErrors.put(node, error);
        GridPane.setHgrow(fieldBox, Priority.ALWAYS);
    }

    private TitledPane group(String title, Region content) {
        TitledPane pane = new TitledPane(title, content);
        pane.setCollapsible(false);
        pane.getStyleClass().add("form-group");
        return pane;
    }

    private void setTabOrder(List<javafx.scene.Node> nodes) {
        for (int i = 0; i < nodes.size(); i++) {
            javafx.scene.Node current = nodes.get(i);
            javafx.scene.Node next = nodes.get((i + 1) % nodes.size());
            current.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, e -> {
                if (e.getCode() == KeyCode.TAB && !e.isShiftDown()) { next.requestFocus(); e.consume(); }
            });
        }
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
