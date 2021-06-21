package controller;

import javafx.animation.TranslateTransition;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import util.FXUtil;

import java.util.ArrayList;
import java.util.List;
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
    public VBox pneVBox;
    public Label lblCaretLocation;
    private int findOffset = -1;

    public void initialize() {
        pneFind.setVisible(false);
        pneReplace.setVisible(false);

        txtEditor.caretPositionProperty().addListener((observable, oldValue, newValue) -> {
//            System.out.println(newValue);
//            System.out.println("a       s d".split("[\\s]+").length);
            String[] rows = (txtEditor.getText()+" ").split("\\n");
            int columnNumber = txtEditor.getCaretPosition();
            System.out.println(columnNumber);
            for (int i = 0; i < rows.length; i++) {
                if (columnNumber <= rows[i].length()) {
                    lblCaretLocation.setText("Line " + (i + 1) + " Col " + (columnNumber + 1));
                    break;
                }
                columnNumber -= rows[i].length() + 1;
            }
            System.out.println(rows.length);
        });

        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            findAll(newValue);
        });

        txtSearchForReplace.textProperty().addListener((observable, oldValue, newValue) -> {
            findAll(newValue);
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
        txtEditor.clear();
        txtEditor.requestFocus();
    }

    public void mnuItemExit_OnAction(ActionEvent actionEvent) {
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
        String output = txtEditor.getText().replaceAll(txtSearchForReplace.getText(), txtReplace.getText());
        txtEditor.setText(output);
        findAll("");
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