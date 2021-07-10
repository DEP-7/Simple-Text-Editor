package controller;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import util.FXUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.prefs.Preferences;
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
    public ToolBar tbStatusBar;
    public Label lblCaretLocation;
    public Label lblWordCount;
    public VBox pneVBox;
    public MenuBar mnuBar;
    private int findOffset = -1;
    private PrinterJob printerJob;
    private Properties prop;

    public void initialize() {
        pneFind.setVisible(false);
        pneReplace.setVisible(false);
        pneVBox.setVisible(false);

        printerJob = PrinterJob.createPrinterJob();
        prop = new Properties();

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

        initializePreferences();
    }

    private void initializePreferences() {
        String backgroundColor = Preferences.userRoot().node("Simple-Text-Editor").get("backgroundColor", "FFFFFF");

        tbStatusBar.setStyle("-fx-background-color:#" + backgroundColor + ";");
        mnuBar.setStyle("-fx-background-color:#" + backgroundColor + ";");

        txtEditor.setStyle(Preferences.userRoot().node("Simple-Text-Editor").get("editorStyles", ""));

        String textStyles = Preferences.userRoot().node("Simple-Text-Editor").get("menuAndStatusBarTextColorStyle", "");
        if (textStyles != "") {
            tbStatusBar.getStylesheets().add(textStyles);
            mnuBar.getStylesheets().add(textStyles);
        }
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

    public void mnuFont_OnAction(ActionEvent actionEvent) throws IOException {
        loadForm("Font");
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

    public void mnuItemNew_OnAction(ActionEvent actionEvent) {
        Optional<ButtonType> option = new Alert(Alert.AlertType.CONFIRMATION, "Do you want to get new page?", ButtonType.YES, ButtonType.NO).showAndWait();
        if (option.get() == ButtonType.YES) {
            txtEditor.clear();
        }
        prop.setProperty("saved.dir", "null");
        txtEditor.requestFocus();
    }

    public void mnuItemExit_OnAction(ActionEvent actionEvent) {
        Optional<ButtonType> option = new Alert(Alert.AlertType.CONFIRMATION, "Do you want to exit?", ButtonType.YES, ButtonType.NO).showAndWait();
        if (option.get() == ButtonType.YES) {
            ((Stage) txtEditor.getScene().getWindow()).close();
        }
    }

    public void mnuItemOpen_OnAction(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All Text Files", "*.txt", "*.html"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All Files", "*"));
        File file = fileChooser.showOpenDialog(txtEditor.getScene().getWindow());

        if (file == null) return;

        prop.setProperty("saved.dir", file.getAbsolutePath());
        txtEditor.clear();
        try (FileReader fileReader = new FileReader(file);
             BufferedReader bufferedReader = new BufferedReader(fileReader)) {

            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                txtEditor.appendText(line + '\n');
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void mnuItemSave_OnAction(ActionEvent actionEvent) {

        File savedFile = prop.getProperty("saved.dir") == null ? null : new File(prop.getProperty("saved.dir"));

        if (prop.getProperty("saved.dir") == null || !savedFile.exists()) {
            saveAsNewFile();
            return;
        }

        try (FileWriter fw = new FileWriter(savedFile);
             BufferedWriter bw = new BufferedWriter(fw)) {

            bw.write(txtEditor.getText());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void mnuItemSaveAs_OnAction(ActionEvent actionEvent) {
        saveAsNewFile();
    }

    private void saveAsNewFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save File");
        File file = fileChooser.showSaveDialog(txtEditor.getScene().getWindow());

        if (file == null) return;

        try (FileWriter fw = new FileWriter(file);
             BufferedWriter bw = new BufferedWriter(fw)) {

            bw.write(txtEditor.getText());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void mnuItemPrint_OnAction(ActionEvent actionEvent) {
        boolean printDialog = printerJob.showPrintDialog(txtEditor.getScene().getWindow());
        if (printDialog) {
            printerJob.printPage(txtEditor.lookup("Text"));
        }
    }

    public void mnuItemPageSetup_OnAction(ActionEvent actionEvent) {
        printerJob.showPageSetupDialog(txtEditor.getScene().getWindow());
    }

    public void mnuItemStatusBar_OnAction(ActionEvent actionEvent) {
        tbStatusBar.setVisible(!tbStatusBar.isVisible());
        AnchorPane.setBottomAnchor(txtEditor, tbStatusBar.isVisible() ? 40.0 : 0.0);
    }

    public void mnuWordWrap_OnAction(ActionEvent actionEvent) {
        txtEditor.setWrapText(!txtEditor.isWrapText());
    }

    public void mnuAppearance_OnAction(ActionEvent actionEvent) {
        loadForm("Appearance");
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
