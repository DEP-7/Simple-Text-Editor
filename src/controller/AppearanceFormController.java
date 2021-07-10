package controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.ColorPicker;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.prefs.Preferences;

public class AppearanceFormController {
    public ColorPicker cpBackgroundColor;
    public ColorPicker cpFontColor;
    private EditorFormController editorFormController;

    public void initialize() {
        Platform.runLater(() -> {
            editorFormController = (EditorFormController) cpFontColor.getScene().getUserData();
            cpBackgroundColor.setValue(Color.web(editorFormController.mnuBar.getStyle().split("#")[1].substring(0, 6)));
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
            Preferences.userRoot().node("Simple-Text-Editor").put("editorStyles", currentStyles.toString());
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

            Preferences.userRoot().node("Simple-Text-Editor").put("editorStyles", currentStyles.toString());

            color = "-fx-background-color:#" + newValue.toString().substring(2) + ";";
            editorFormController.tbStatusBar.setStyle(color);
            editorFormController.mnuBar.setStyle(color);

            Preferences.userRoot().node("Simple-Text-Editor").put("backgroundColor",newValue.toString().substring(2));

            int colorTotal = 0;
            for (int i = 1; i < 4; i++) {
                colorTotal += Integer.parseInt(newValue.toString().substring(i * 2, (i + 1) * 2), 16);
            }

            if (colorTotal < 210) {
                editorFormController.tbStatusBar.getStylesheets().add("view/css/EditorFormStyles.css");
                editorFormController.mnuBar.getStylesheets().add("view/css/EditorFormStyles.css");

                Preferences.userRoot().node("Simple-Text-Editor").put("menuAndStatusBarTextColorStyle","view/css/EditorFormStyles.css");
            } else {
                editorFormController.mnuBar.getStylesheets().clear();
                editorFormController.tbStatusBar.getStylesheets().clear();

                Preferences.userRoot().node("Simple-Text-Editor").put("menuAndStatusBarTextColorStyle","");
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
