import controller.EditorFormController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.prefs.Preferences;

public class AppInitializer extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("view/EditorForm.fxml"));
        AnchorPane root = fxmlLoader.load();
        Scene mainScene = new Scene(root);
        primaryStage.setScene(mainScene);
        EditorFormController controller = fxmlLoader.getController();
        mainScene.setUserData(controller);
        primaryStage.setTitle("Simple Text Editor");
        primaryStage.setMinHeight(180);
        primaryStage.setMinWidth(500);

        boolean isMaximized = Preferences.userRoot().node("Simple-Text-Editor").getBoolean("isMaximized", false);
        double posX = Preferences.userRoot().node("Simple-Text-Editor").getDouble("posX", -1);
        double posY = Preferences.userRoot().node("Simple-Text-Editor").getDouble("posY", -1);
        double width = Preferences.userRoot().node("Simple-Text-Editor").getDouble("width", -1);
        double height = Preferences.userRoot().node("Simple-Text-Editor").getDouble("height", -1);

        primaryStage.setMaximized(isMaximized);

        if (!isMaximized) {
            if (width == -1 && height == -1) {
                primaryStage.setWidth(root.getPrefWidth());
                primaryStage.setHeight(root.getPrefHeight());
            } else {
                primaryStage.setWidth(width);
                primaryStage.setHeight(height);
            }

            if (posX == -1 && posY == -1) {
                primaryStage.centerOnScreen();
            } else {
                primaryStage.setX(posX);
                primaryStage.setY(posY);
            }
        }

        primaryStage.show();
    }
}
