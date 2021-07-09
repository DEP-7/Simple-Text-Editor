package controller;

import com.sun.javafx.css.Stylesheet;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.ColorPicker;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class AppearanceFormController {
    public ColorPicker cpBackgroundColor;
    public ColorPicker cpFontColor;
    private EditorFormController editorFormController;

    public void initialize() {
        Platform.runLater(() -> {
            editorFormController = (EditorFormController) cpFontColor.getScene().getUserData();
        });

        cpFontColor.valueProperty().addListener((observable, oldValue, newValue) -> {
            String color = "-fx-text-fill:#" + newValue.toString().substring(2) + ";";
            StringBuilder currentStyles = new StringBuilder(editorFormController.txtEditor.getStyle());

            if (currentStyles.toString().contains("-fx-text-fill:#")) {
                int startingIndex = currentStyles.indexOf("-fx-text-fill:#");
                currentStyles.delete(startingIndex, currentStyles.indexOf(";", startingIndex) + 1);
            }

            currentStyles.append(color);

            editorFormController.txtEditor.setStyle(currentStyles.toString());
        });

        cpBackgroundColor.valueProperty().addListener((observable, oldValue, newValue) -> {
            String color = "-fx-base:#" + newValue.toString().substring(2) + ";";
            StringBuilder currentStyles = new StringBuilder(editorFormController.txtEditor.getStyle());

            if (currentStyles.toString().contains("-fx-base:#")) {
                int startingIndex = currentStyles.indexOf("-fx-base:#");
                currentStyles.delete(startingIndex, currentStyles.indexOf(";", startingIndex) + 1);
            }

            currentStyles.append(color);

            editorFormController.txtEditor.setStyle(currentStyles.toString());
            color = "-fx-background-color:#" + newValue.toString().substring(2)+";";
            editorFormController.tbStatusBar.setStyle(color);
            editorFormController.mnuBar.setStyle(color);

            int colorTotal = 0;
            for (int i = 1; i < 4; i++) {
                colorTotal += Integer.parseInt(newValue.toString().substring(i * 2, (i + 1) * 2), 16);
            }
            System.out.println(newValue);
            System.out.println(colorTotal);

            if (colorTotal < 210) {
                editorFormController.tbStatusBar.getStylesheets().add("view/css/EditorFormStyles.css");
                editorFormController.mnuBar.getStylesheets().add("view/css/EditorFormStyles.css");
            }else{
                /*editorFormController.tbStatusBar.getItems().get(0).setStyle("-fx-text-fill:#000000;");
                editorFormController.tbStatusBar.getItems().get(1).setStyle("-fx-text-fill:#000000;");*/
                editorFormController.mnuBar.getStylesheets().clear();
                editorFormController.tbStatusBar.getStylesheets().clear();
            }
        });
    }

    public void btnResetFont_OnAction(ActionEvent actionEvent) {
        cpFontColor.setValue(Color.BLACK);
        cpBackgroundColor.setValue(Color.WHITE);
    }

    public void btnResetFont_OnKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER || keyEvent.getCode() == KeyCode.SPACE) {
            cpFontColor.setValue(Color.BLACK);
            cpBackgroundColor.setValue(Color.WHITE);
        }
    }

    public void btnOk_OnAction(ActionEvent actionEvent) {
        ((Stage) cpBackgroundColor.getScene().getWindow()).close();
    }

    public void btnOk_OnKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER || keyEvent.getCode() == KeyCode.SPACE) {
            ((Stage) cpBackgroundColor.getScene().getWindow()).close();
        }
    }
}
