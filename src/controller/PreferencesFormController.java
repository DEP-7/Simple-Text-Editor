package controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.Window;

public class PreferencesFormController {
    public ListView<Integer> lstFontSize;
    public ListView<String> lstFontStyle;
    public ListView<Font> lstFont;
    public TextField txtFontStyle;
    public TextField txtFontSize;
    public TextField txtFont;
    public Label lblSample;
    EditorFormController editorFormController;

    public void initialize() {
        Platform.runLater(() -> {
            editorFormController = (EditorFormController) txtFont.getScene().getUserData();
            int increment = 1;

            for (int i = 8; i <= 48; i += increment) {
                lstFontSize.getItems().add(i);
                if (i == 12) {
                    increment = 2;
                } else if (i == 24) {
                    increment = 4;
                }
            }

            lstFontSize.getSelectionModel().select(new Integer((int)editorFormController.txtEditor.getFont().getSize()));
            txtFontSize.setText((int)editorFormController.txtEditor.getFont().getSize()+"");
        });

        lstFontSize.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            txtFontSize.setText(newValue+"");
        });
    }

    public void btnResetFont_OnAction(ActionEvent actionEvent) {
        lstFontSize.getSelectionModel().select(new Integer(14));
    }

    public void btnResetFont_OnKeyPressed(KeyEvent keyEvent) {
    }

    public void btnOk_OnAction(ActionEvent actionEvent) {
        editorFormController.txtEditor.setFont(Font.font(Double.parseDouble(txtFontSize.getText())));
        Stage window = (Stage) txtFontSize.getScene().getWindow();
        window.close();
    }

    public void btnOk_OnKeyPressed(KeyEvent keyEvent) {
    }

    public void btnCancel_OnAction(ActionEvent actionEvent) {
        Stage window = (Stage) txtFontSize.getScene().getWindow();
        window.close();
    }

    public void btnCancel_OnKeyPressed(KeyEvent keyEvent) {
    }
}
