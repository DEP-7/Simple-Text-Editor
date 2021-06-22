package controller;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import util.FXUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class EditorFormController {
    private final List<Index> searchList = new ArrayList<>();
    public AnchorPane pneReplace;
    public AnchorPane pneFind;
    public TextField txtSearchForReplace;
    public TextField txtReplace;
    public TextField txtSearch;
    public TextArea txtEditor;
    public Label lblCaretLocation;
    public Label lblWordCount;
    public VBox pneVBox;
    private int findOffset = -1;

    public void initialize() {
        pneFind.setVisible(false);
        pneReplace.setVisible(false);
        pneVBox.setVisible(false);

        setWordCount();

        txtEditor.caretPositionProperty().addListener((observable, oldValue, newValue) -> {
            String[] rows = (txtEditor.getText() + " ").split("\\n");
            int columnNumber = txtEditor.getCaretPosition();

            for (int i = 0; i < rows.length; i++) {
                if (columnNumber <= rows[i].length()) {
                    lblCaretLocation.setText("Line " + (i + 1) + " Col " + (columnNumber + 1));
                    break;
                }
                columnNumber -= rows[i].length() + 1;
            }
        });

        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            findAll(newValue);
        });

        txtSearchForReplace.textProperty().addListener((observable, oldValue, newValue) -> {
            findAll(newValue);
        });

        txtEditor.textProperty().addListener(observable -> {
            setWordCount();
        });

        txtEditor.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                findAll("");
                AnchorPane children = (AnchorPane) pneVBox.getChildren().get(0);

                if (children.isVisible()) {
                    showSearchBar(children);
                }
            }
        });

        Platform.runLater(() -> {
            Stage stage = (Stage) txtEditor.getScene().getWindow();

            stage.setOnCloseRequest(event -> {
                Optional<ButtonType> option = new Alert(Alert.AlertType.CONFIRMATION, "Do you want to exit?", ButtonType.YES, ButtonType.NO).showAndWait();

                if (option.get() == ButtonType.NO) {
                    event.consume();
                }

            });
        });
    }

    private void setWordCount() {
        String text = txtEditor.getText().isEmpty() ? "0 Words, " : txtEditor.getText().split("[\\n\\s]+").length + " Words, ";
        text += txtEditor.getText().replaceAll("[\\n]+", "").length() + " Characters";
        lblWordCount.setText(text);
    }

    private void findAll(String newValue) {
        FXUtil.highlightOnTextArea(txtEditor, newValue, Color.web("yellow", 0.8));

        try {
            Pattern regExp = Pattern.compile(newValue);
            Matcher matcher = regExp.matcher(txtEditor.getText());

            searchList.clear();

            while (matcher.find()) {
                searchList.add(new Index(matcher.start(), matcher.end()));
            }

            for (Index index : searchList) {
                if (index.startingIndex > txtEditor.getCaretPosition()) {
                    findOffset = searchList.indexOf(index) - 1;
                    break;
                }
            }
        } catch (PatternSyntaxException e) {
        }
    }


    public void mnuItemNew_OnAction(ActionEvent actionEvent) {
        Optional<ButtonType> option = new Alert(Alert.AlertType.CONFIRMATION, "Do you want to get new page?", ButtonType.YES, ButtonType.NO).showAndWait();
        if (option.get() == ButtonType.YES) {
            txtEditor.clear();
        }
        txtEditor.requestFocus();
    }

    public void mnuItemExit_OnAction(ActionEvent actionEvent) {
        Optional<ButtonType> option = new Alert(Alert.AlertType.CONFIRMATION, "Do you want to exit?", ButtonType.YES, ButtonType.NO).showAndWait();
        if (option.get() == ButtonType.YES) {
            ((Stage) txtEditor.getScene().getWindow()).close();
        }
    }

    public void mnuItemFind_OnAction(ActionEvent actionEvent) {
        showSearchBar(pneFind);
    }

    public void mnuItemReplace_OnAction(ActionEvent actionEvent) {
        showSearchBar(pneReplace);
    }

    private void showSearchBar(AnchorPane anchorPane) {
        ObservableList<Node> children = pneVBox.getChildren();

        if (children.get(0) == anchorPane) {
            boolean focusChanged = false;
            if (!children.get(0).isVisible()) {
                focusChanged = true;
                children.get(0).setVisible(true);
                pneVBox.setVisible(true);
                TextField textField = (TextField) ((AnchorPane) children.get(0)).getChildren().get(0);
                textField.requestFocus();
                findAll(textField.getText());
            }

            double paneHeight = ((AnchorPane) children.get(0)).getHeight();
            TranslateTransition paneAppearAnimation = new TranslateTransition(new Duration((paneHeight + 100) / 2 * 3), children.get(0));
            paneAppearAnimation.setFromY(focusChanged ? -paneHeight - 29 : -29);
            paneAppearAnimation.setToY(focusChanged ? -29 : -paneHeight - 29);
            paneAppearAnimation.play();

            if (!focusChanged) {
                paneAppearAnimation.setOnFinished(event -> {
                    children.get(0).setVisible(false);
                    pneVBox.setVisible(false);
                });
            }
            return;
        }

        pneVBox.getChildren().clear();
        pneVBox.getChildren().add(anchorPane);
        showSearchBar(anchorPane);
    }

    public void mnuItemSelectAll_OnAction(ActionEvent actionEvent) {
        txtEditor.selectAll();
    }

    public void btnFindNext_OnAction(ActionEvent actionEvent) {
        forwardSearch(true);
    }

    public void btnFindPrevious_OnAction(ActionEvent actionEvent) {
        forwardSearch(false);
    }

    public void btnFind_OnAction(ActionEvent actionEvent) {
        forwardSearch(true);
    }

    private void forwardSearch(boolean value) {
        int toggle = value ? 1 : -1;

        if (!searchList.isEmpty()) {
            findOffset += toggle;
            if (findOffset >= searchList.size()) {
                findOffset = 0;
            } else if (findOffset < 0) {
                findOffset = searchList.size() - 1;
            }
            txtEditor.selectRange(searchList.get(findOffset).startingIndex, searchList.get(findOffset).endIndex);
        }
    }

    public void btnReplace_OnAction(ActionEvent actionEvent) {
        if (txtEditor.getSelectedText().isEmpty()) {
            forwardSearch(true);
            return;
        }
        int caretPosition = txtEditor.getCaretPosition() - txtEditor.getSelectedText().length() + txtReplace.getText().length();
        String replacedText = txtEditor.getText().substring(0, searchList.get(findOffset).startingIndex) + txtReplace.getText() + txtEditor.getText().substring(searchList.get(findOffset).endIndex);
        txtEditor.setText(replacedText);
        txtEditor.positionCaret(caretPosition);
        findAll(txtSearchForReplace.getText());
        forwardSearch(true);
    }

    public void btnReplaceAll_OnAction(ActionEvent actionEvent) {
        if (txtSearchForReplace.getText().isEmpty()) {
            txtSearchForReplace.requestFocus();
            return;
        }
        String selectedText = txtEditor.getSelectedText();
        int caretPosition = txtEditor.getCaretPosition();
        String firstPart = txtEditor.getText().substring(0, caretPosition).replaceAll(txtSearchForReplace.getText(), txtReplace.getText());
        String secondPart = txtEditor.getText().substring(caretPosition).replaceAll(txtSearchForReplace.getText(), txtReplace.getText());
        selectedText = selectedText.replaceAll(txtSearchForReplace.getText(), txtReplace.getText());
        txtEditor.setText(firstPart + secondPart);
        findAll("");

        if (!selectedText.isEmpty()) {

            if (firstPart.endsWith(selectedText)) {
                txtEditor.selectRange(firstPart.length() - selectedText.length(), firstPart.length());
            } else if (secondPart.startsWith(selectedText)) {
                txtEditor.selectRange(firstPart.length(), firstPart.length() + selectedText.length());
            }

        }

    }

    public void mnuPreferences_OnAction(ActionEvent actionEvent) throws IOException {
        loadForm("Preferences");
    }

    public void mnuItemAbout_OnAction(ActionEvent actionEvent) {
        loadForm("About");
    }

    private void loadForm(String formName) {
        try {
            Stage childStage = new Stage();
            Parent root = FXMLLoader.load(this.getClass().getResource("../view/" + formName + "Form.fxml"));
            Scene childScene = new Scene(root);
            EditorFormController ctrl = (EditorFormController) txtEditor.getScene().getUserData();
            childScene.setUserData(ctrl);
            childStage.setScene(childScene);
            childStage.setTitle(formName);
            childStage.initModality(Modality.WINDOW_MODAL);
            childStage.initOwner(txtEditor.getScene().getWindow());
            childStage.setResizable(false);
            childStage.centerOnScreen();
            childStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void mnuItemCut_OnAction(ActionEvent actionEvent) {
        txtEditor.cut();
    }

    public void mnuItemCopy_OnAction(ActionEvent actionEvent) {
        txtEditor.copy();
    }

    public void mnuItemPaste_OnAction(ActionEvent actionEvent) {
        txtEditor.paste();
    }

    public void mnuItemUndo_OnAction(ActionEvent actionEvent) {
        txtEditor.undo();
    }

    public void mnuItemRedo_OnAction(ActionEvent actionEvent) {
        txtEditor.redo();
    }
}

class Index {
    int startingIndex;
    int endIndex;

    public Index(int startingIndex, int endIndex) {
        this.startingIndex = startingIndex;
        this.endIndex = endIndex;
    }
}
