package app;

import com.hci.scholarship.app.MainApp;
import javafx.stage.Stage;

public class Router {
    private static Stage stage;

    private Router() {}

    public static void setStage(Stage stage) {
        Router.stage = stage;
    }

    public static void navigateTo(ViewEnum view) {
        if (stage == null) {
            throw new IllegalStateException("Stage is not initialized.");
        }
        try {
            switch (view) {
                case SCHOLARSHIP_PORTAL -> new MainApp().start(stage);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to open view: " + view, e);
        }
    }
}
