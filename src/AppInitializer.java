import controller.EditorFormController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class AppInitializer extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("view/EditorForm.fxml"));
        Scene mainScene = new Scene(fxmlLoader.load());
        primaryStage.setScene(mainScene);
        EditorFormController controller = fxmlLoader.getController();
        mainScene.setUserData(controller);
        primaryStage.setTitle("Simple Text Editor");
        primaryStage.setMinHeight(180);
        primaryStage.setMinWidth(500);
        //primaryStage.setMaximized(true);
        primaryStage.show();
    }
}
