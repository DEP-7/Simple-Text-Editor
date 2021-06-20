package controller;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
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
    private int findOffset = -1;

    public void initialize() {
        pneFind.setVisible(false);
        pneReplace.setVisible(false);

        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            findAll(newValue);
        });

        txtSearchForReplace.textProperty().addListener((observable, oldValue, newValue) -> {
            findAll(newValue);
        });

        txtEditor.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                findAll("");
                pneVBox.getChildren().get(0).setVisible(false);
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
            findOffset = -1;
            txtEditor.deselect();
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
        viewSearchOrReplace(pneFind);
    }

    public void mnuItemReplace_OnAction(ActionEvent actionEvent) {
        viewSearchOrReplace(pneReplace);
    }

    private void viewSearchOrReplace(AnchorPane anchorPane) {
        ObservableList<Node> children = pneVBox.getChildren();
        if (children.get(0) == anchorPane) {
            children.get(0).setVisible(true);
            TextField textField = (TextField) ((AnchorPane) children.get(0)).getChildren().get(0);
            textField.requestFocus();
            findAll(textField.getText());
            return;
        }
        pneVBox.getChildren().clear();
        pneVBox.getChildren().add(anchorPane);
        viewSearchOrReplace(anchorPane);
    }

    public void mnuItemSelectAll_OnAction(ActionEvent actionEvent) {
        txtEditor.selectAll();
    }

    public void btnFindNext_OnAction(ActionEvent actionEvent) {
        find(true);
    }

    public void btnFindPrevious_OnAction(ActionEvent actionEvent) {
        find(false);
    }

    public void btnFind_OnAction(ActionEvent actionEvent) {
        find(true);
    }

    private void find(boolean forwardSearch) {
        int toggle = forwardSearch ? 1 : -1;

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
            find(true);
            System.out.println(txtEditor.getCaretPosition());
            return;
        }
        System.out.println(txtEditor.getCaretPosition());
        String replacedText = txtEditor.getText().substring(0, searchList.get(findOffset).startingIndex)+txtEditor.getText().substring(searchList.get(findOffset).endIndex);
        txtEditor.setText(replacedText);
        System.out.println(txtEditor.getCaretPosition());
        findAll(txtSearchForReplace.getText());
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