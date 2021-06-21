package controller;

import com.sun.org.apache.xerces.internal.impl.xpath.regex.RegularExpression;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class PreferencesFormController {
    public ListView<Integer> lstFontSize;
    public ListView<Text> lstFontStyle;
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

            lstFontSize.getSelectionModel().select(new Integer((int) editorFormController.txtEditor.getFont().getSize()));
            txtFontSize.setText((int) editorFormController.txtEditor.getFont().getSize() + "");

            Text regular = new Text("Regular");
            Text bold = new Text("Bold");
            bold.setFont(Font.font("System",FontWeight.BOLD,13));
            Text italic = new Text("Italic");
            italic.setFont(Font.font("System", FontPosture.ITALIC,13));
            Text boldItalic = new Text("Bold Italic");
            boldItalic.setFont(Font.font("System",FontWeight.BOLD,FontPosture.ITALIC,13));
            lstFontStyle.getItems().setAll(regular, bold, italic, boldItalic);

            if (editorFormController.txtEditor.getFont().getStyle().equals("Regular")) {
                lstFontStyle.getSelectionModel().select(regular);
            }else if (editorFormController.txtEditor.getFont().getStyle().equals("Bold")) {
                lstFontStyle.getSelectionModel().select(bold);
            }
            else if (editorFormController.txtEditor.getFont().getStyle().equals("Italic")) {
                lstFontStyle.getSelectionModel().select(italic);
            }else{
                lstFontStyle.getSelectionModel().select(boldItalic);
            }

            txtFontStyle.setText(lstFontStyle.getSelectionModel().getSelectedItem().getText());
        });

        lstFontSize.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            txtFontSize.setText(newValue + "");
        });

        lstFontStyle.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            txtFontStyle.setText(newValue.getText());
        });
    }

    public void btnResetFont_OnAction(ActionEvent actionEvent) {
        lstFontSize.getSelectionModel().select(new Integer(14));
        lstFontStyle.getSelectionModel().select(0);
    }

    public void btnResetFont_OnKeyPressed(KeyEvent keyEvent) {
    }

    public void btnOk_OnAction(ActionEvent actionEvent) {
        if (txtFontStyle.getText().equals("Regular")) {
            editorFormController.txtEditor.setFont(Font.font(Double.parseDouble(txtFontSize.getText())));
        }else if (txtFontStyle.getText().equals("Bold")) {
            editorFormController.txtEditor.setFont(Font.font("System",FontWeight.BOLD,Double.parseDouble(txtFontSize.getText())));
        }else if (txtFontStyle.getText().equals("Italic")) {
            editorFormController.txtEditor.setFont(Font.font("System",FontWeight.NORMAL,FontPosture.ITALIC,Double.parseDouble(txtFontSize.getText())));
        }else{
            editorFormController.txtEditor.setFont(Font.font("System",FontWeight.BOLD,FontPosture.ITALIC,Double.parseDouble(txtFontSize.getText())));
        }
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
}
