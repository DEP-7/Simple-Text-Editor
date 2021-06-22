package controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.List;

public class PreferencesFormController {
    public ListView<Integer> lstFontSize;
    public ListView<Text> lstFontStyle;
    public ListView<Font> lstFont;
    public TextField txtFontStyle;
    public TextField txtFontSize;
    public TextField txtSample;
    public TextField txtFont;
    EditorFormController editorFormController;

    public void initialize() {
        Platform.runLater(() -> {
            editorFormController = (EditorFormController) txtFont.getScene().getUserData();
            loadFontSizes();
            loadFontStyles();
        });

        lstFontSize.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            txtFontSize.setText(newValue + "");
            setFont(txtSample, Double.parseDouble(txtFontSize.getText()), (txtFontStyle.getText()));
        });

        lstFontStyle.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            txtFontStyle.setText(newValue.getText());
            setFont(txtSample, Double.parseDouble(txtFontSize.getText()), newValue.getText());
        });
    }

    private void loadFontStyles() {
        List<String> fontStyleNames = FXCollections.observableArrayList("Regular", "Bold", "Italic", "Bold Italic");
        ObservableList<Text> fontStyleText = FXCollections.observableArrayList(new Text("Regular"), new Text("Bold"), new Text("Italic"), new Text("Bold Italic"));
        fontStyleText.get(1).setFont(Font.font("System", FontWeight.BOLD, 13));
        fontStyleText.get(2).setFont(Font.font("System", FontPosture.ITALIC, 13));
        fontStyleText.get(3).setFont(Font.font("System", FontWeight.BOLD, FontPosture.ITALIC, 13));
        lstFontStyle.getItems().setAll(fontStyleText);

        lstFontStyle.getSelectionModel().select(fontStyleNames.indexOf(editorFormController.txtEditor.getFont().getStyle()));
    }

    private void loadFontSizes() {
        int increment = 1;

        for (int i = 8; i <= 48; i += increment) {
            lstFontSize.getItems().add(i);
            if (i == 12) {
                increment = 2;
            } else if (i == 24) {
                increment = 4;
            }
        }

        lstFontSize.getSelectionModel().select(new Integer((int) editorFormController.txtEditor.getFont().getSize()));
        txtFontSize.setText((int) editorFormController.txtEditor.getFont().getSize() + "");
    }

    public void btnResetFont_OnAction(ActionEvent actionEvent) {
        lstFontSize.getSelectionModel().select(new Integer(14));
        lstFontStyle.getSelectionModel().select(0);
    }

    public void btnResetFont_OnKeyPressed(KeyEvent keyEvent) {
    }

    public void btnOk_OnAction(ActionEvent actionEvent) {
        setFont(editorFormController.txtEditor, Double.parseDouble(txtFontSize.getText()), txtFontStyle.getText());
        ((Stage) txtFontSize.getScene().getWindow()).close();
    }

    public void btnOk_OnKeyPressed(KeyEvent keyEvent) {
    }

    public void btnCancel_OnAction(ActionEvent actionEvent) {
        ((Stage) txtFontSize.getScene().getWindow()).close();
    }

    public void btnCancel_OnKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER || keyEvent.getCode() == KeyCode.SPACE) {
            ((Stage) txtFontSize.getScene().getWindow()).close();
        }
    }

    private void setFont(TextInputControl textField, double fontSize, String fontStyle) {
        switch (fontStyle) {
            case "Bold":
                textField.setFont(Font.font("System", FontWeight.BOLD, fontSize));
                break;
            case "Italic":
                textField.setFont(Font.font("System", FontWeight.NORMAL, FontPosture.ITALIC, fontSize));
                break;
            case "Bold Italic":
                textField.setFont(Font.font(fontSize));
                break;
            default:
                textField.setFont(Font.font("System", FontWeight.BOLD, FontPosture.ITALIC, fontSize));
                break;
        }
    }
}
