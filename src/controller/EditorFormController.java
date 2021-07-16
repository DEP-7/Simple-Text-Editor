package controller;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
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
    public CheckBox chkMatchCase_Replace;
    public CheckBox chkMatchCase_Find;
    public TextArea txtEditor;
    public ToolBar tbStatusBar;
    public Label lblCaretLocation;
    public Label lblWordCount;
    public VBox pneVBox;
    public MenuBar mnuBar;
    public Label lblSearchCount;
    private int findOffset = -1;
    private PrinterJob printerJob;
    private boolean changeDetected = false;
    private Stage primaryStage;

    public void initialize() {
        pneFind.setVisible(false);
        pneReplace.setVisible(false);
        pneVBox.setVisible(false);

        printerJob = PrinterJob.createPrinterJob();

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
            findAll(newValue, chkMatchCase_Find.isSelected());
        });

        txtSearchForReplace.textProperty().addListener((observable, oldValue, newValue) -> {
            findAll(newValue, chkMatchCase_Replace.isSelected());
        });

        txtEditor.textProperty().addListener(observable -> {
            setWordCount();
            changeDetected = true;
        });

        chkMatchCase_Find.selectedProperty().addListener((observable, oldValue, newValue) -> {
            findAll(txtSearch.getText(),newValue);
        });

        chkMatchCase_Replace.selectedProperty().addListener((observable, oldValue, newValue) -> {
            findAll(txtSearchForReplace.getText(),newValue);
        });

        txtEditor.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                findAll("", false);
                AnchorPane children = (AnchorPane) pneVBox.getChildren().get(0);

                if (children.isVisible()) {
                    showSearchBar(children);
                }
            }
        });

        Platform.runLater(() -> {
            primaryStage = (Stage) txtEditor.getScene().getWindow();
            primaryStage.setOnCloseRequest(event -> {
                Preferences.userRoot().node("Simple-Text-Editor").putDouble("posX", primaryStage.getX());
                Preferences.userRoot().node("Simple-Text-Editor").putDouble("posY", primaryStage.getY());
                Preferences.userRoot().node("Simple-Text-Editor").putDouble("width", primaryStage.getWidth());
                Preferences.userRoot().node("Simple-Text-Editor").putDouble("height", primaryStage.getHeight() - 37);
                Preferences.userRoot().node("Simple-Text-Editor").put("savedLocation", "");

                if (changeDetected) {
                    ButtonType save = new ButtonType("Save");
                    ButtonType dontSave = new ButtonType("Don't Save");
                    Optional<ButtonType> option = new Alert(Alert.AlertType.CONFIRMATION, "Do you want to save changes to " + primaryStage.getTitle() + "?", save, dontSave, ButtonType.CANCEL).showAndWait();
                    if (option.get() == save) {
                        saveFile();
                    } else if (option.get() == ButtonType.CANCEL) {
                        event.consume();
                    }
                }
            });
        });

        initializePreferences();
    }

    private void initializePreferences() {
        String backgroundColor = Preferences.userRoot().node("Simple-Text-Editor").get("backgroundColor", "FFFFFF");
        String fontColor = Preferences.userRoot().node("Simple-Text-Editor").get("fontColor", "000000");

        tbStatusBar.setStyle("-fx-background-color:#" + backgroundColor + ";");
        mnuBar.setStyle("-fx-background-color:#" + backgroundColor + ";");
        txtEditor.setStyle("-fx-text-fill:#" + fontColor + ";" + "-fx-base:#" + backgroundColor + ";");

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

    private void findAll(String value, boolean matchCase) {
        if (!matchCase) {
            value = value.toLowerCase();
        }

        FXUtil.highlightOnTextArea(txtEditor, value, matchCase, Color.web("yellow", 0.8));

        if (value.isEmpty()) {
            findOffset = -1;
            searchList.clear();
            lblSearchCount.setText("Search here to find");
            return;
        }

        try {
            Pattern regExp = Pattern.compile(value);
            Matcher matcher = regExp.matcher(matchCase ? txtEditor.getText() : txtEditor.getText().toLowerCase());

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

            lblSearchCount.setText(searchList.size() + " results found");
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
                findAll(textField.getText(), anchorPane == pneFind ? chkMatchCase_Find.isSelected() : chkMatchCase_Replace.isSelected());
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
        if (!txtEditor.getSelectedText().isEmpty()) {
            lblSearchCount.setText((findOffset + 1) + " of " + searchList.size());
        } else {
            lblSearchCount.setText(searchList.size() + "");
        }
    }

    public void btnFindPrevious_OnAction(ActionEvent actionEvent) {
        forwardSearch(false);
        if (!txtEditor.getSelectedText().isEmpty()) {
            lblSearchCount.setText((findOffset + 1) + " of " + searchList.size());
        } else {
            lblSearchCount.setText(searchList.size() + "");
        }
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
        findAll(txtSearchForReplace.getText(), chkMatchCase_Replace.isSelected());
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
        findAll("", false);

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
            AnchorPane root = FXMLLoader.load(this.getClass().getResource("../view/" + formName + "Form.fxml"));
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
        if (changeDetected) {
            ButtonType save = new ButtonType("Save");
            ButtonType dontSave = new ButtonType("Don't Save");
            Optional<ButtonType> option = new Alert(Alert.AlertType.CONFIRMATION, "Do you want to save changes to " + primaryStage.getTitle() + "?", save, dontSave, ButtonType.CANCEL).showAndWait();
            if (option.get() == save) {
                if (!saveFile()) {
                    return;
                }
            } else if (option.get() == ButtonType.CANCEL) {
                return;
            }
        }

        txtEditor.clear();
        Preferences.userRoot().node("Simple-Text-Editor").put("savedLocation", "");
        txtEditor.requestFocus();
        changeDetected = false;
    }

    public void mnuItemExit_OnAction(ActionEvent actionEvent) {
        Optional<ButtonType> option = new Alert(Alert.AlertType.CONFIRMATION, "Do you want to exit?", ButtonType.YES, ButtonType.NO).showAndWait();
        if (option.get() == ButtonType.YES) {
            primaryStage.close();
        }
    }

    public void mnuItemOpen_OnAction(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All Text Files", "*.txt", "*.html"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All Files", "*"));
        File file = fileChooser.showOpenDialog(txtEditor.getScene().getWindow());
        openFile(file);
    }

    private void openFile(File file) {

        if (file == null) return;

        if (changeDetected) {
            ButtonType save = new ButtonType("Save");
            ButtonType dontSave = new ButtonType("Don't Save");
            Optional<ButtonType> option = new Alert(Alert.AlertType.CONFIRMATION, "Do you want to save changes to " + primaryStage.getTitle() + "?", save, dontSave, ButtonType.CANCEL).showAndWait();
            if (option.get() == save) {
                saveFile();
            } else if (option.get() == ButtonType.CANCEL) {
                return;
            }
        }
        Preferences.userRoot().node("Simple-Text-Editor").put("savedLocation", file.getAbsolutePath());
        txtEditor.clear();

        try (FileReader fileReader = new FileReader(file);
             BufferedReader bufferedReader = new BufferedReader(fileReader)) {

            String line;
            boolean isFirstLine = true;
            while ((line = bufferedReader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    txtEditor.appendText(line);
                } else {
                    txtEditor.appendText('\n' + line);
                }
            }
            changeDetected = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void mnuItemSave_OnAction(ActionEvent actionEvent) {
        saveFile();
    }

    private boolean saveFile() {
        String savedLocation = Preferences.userRoot().node("Simple-Text-Editor").get("savedLocation", "");
        File savedFile = new File(savedLocation);

        if (savedLocation.equals("") || !savedFile.exists()) {
            return saveAsNewFile("Save");
        }

        try (FileWriter fw = new FileWriter(savedFile);
             BufferedWriter bw = new BufferedWriter(fw)) {

            bw.write(txtEditor.getText());
            changeDetected = false;
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void mnuItemSaveAs_OnAction(ActionEvent actionEvent) {
        saveAsNewFile("Save As");
    }

    private boolean saveAsNewFile(String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        File file = fileChooser.showSaveDialog(txtEditor.getScene().getWindow());

        if (file == null) return false;

        try (FileWriter fw = new FileWriter(file);
             BufferedWriter bw = new BufferedWriter(fw)) {

            bw.write(txtEditor.getText());
            changeDetected = false;
            Preferences.userRoot().node("Simple-Text-Editor").put("savedLocation", file.getAbsolutePath());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void mnuItemPrint_OnAction(ActionEvent actionEvent) {
        try {
            boolean printDialog = printerJob.showPrintDialog(txtEditor.getScene().getWindow());
            if (printDialog) {
                printerJob.printPage(txtEditor.lookup("Text"));
            }
        } catch (RuntimeException e) {
            new Alert(Alert.AlertType.ERROR, "Couldn't find a printer.\nPlease set default printer").show();
            txtEditor.requestFocus();
        }
    }

    public void mnuItemPageSetup_OnAction(ActionEvent actionEvent) {
        try {
            printerJob.showPageSetupDialog(primaryStage);
        } catch (RuntimeException e) {
            new Alert(Alert.AlertType.ERROR, "Couldn't find a printer.\nPlease set default printer").show();
            txtEditor.requestFocus();
        }
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

    public void txtEditor_OnDragOver(DragEvent dragEvent) {
        if (dragEvent.getDragboard().hasFiles()) {
            dragEvent.acceptTransferModes(TransferMode.ANY);
            dragEvent.consume();
        }
    }

    public void txtEditor_OnDragDropped(DragEvent dragEvent) {
        if (dragEvent.getDragboard().hasFiles()) {
            File file = dragEvent.getDragboard().getFiles().get(0);
            openFile(file);
            dragEvent.setDropCompleted(true);
        }
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
