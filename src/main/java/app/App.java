package app;

import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) {
        Router.setStage(stage);
        Router.navigateTo(ViewEnum.SCHOLARSHIP_PORTAL);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
