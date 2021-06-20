package controller;

import javafx.collections.FXCollections;
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
    public AnchorPane pneReplace;
    public AnchorPane pneFind;
    public TextField txtSearchForReplace;
    public TextField txtReplace;
    public TextField txtSearch;
    public TextArea txtEditor;
    public VBox pneVBox;
    private int findOffset = -1;
    private final List<Index> searchList = new ArrayList<>();

    public void initialize() {
        pneFind.setVisible(false);
        pneReplace.setVisible(false);

        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            FXUtil.highlightOnTextArea(txtEditor,newValue, Color.web("yellow", 0.8));

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
        });

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

    private void viewSearchOrReplace(AnchorPane anchorPane){
        ObservableList<Node> children = pneVBox.getChildren();
        if (children.get(0) != anchorPane) {
            FXCollections.reverse(children);
        }
        children.get(0).setVisible(true);
        children.get(1).setVisible(false);
        ((AnchorPane)children.get(0)).getChildren().get(0).requestFocus();
    }

    public void mnuItemSelectAll_OnAction(ActionEvent actionEvent) {
        txtEditor.selectAll();
    }

    public void btnFindNext_OnAction(ActionEvent actionEvent) {
        if (!searchList.isEmpty()) {
            findOffset++;
            if (findOffset >= searchList.size()) {
                findOffset = 0;
            }
            txtEditor.selectRange(searchList.get(findOffset).startingIndex, searchList.get(findOffset).endIndex);
        }
    }

    public void btnFindPrevious_OnAction(ActionEvent actionEvent) {
        if (!searchList.isEmpty()) {
            findOffset--;
            if (findOffset < 0) {
                findOffset = searchList.size() - 1;
            }
            txtEditor.selectRange(searchList.get(findOffset).startingIndex, searchList.get(findOffset).endIndex);
        }
    }

    public void btnFind_OnAction(ActionEvent actionEvent) {
    }

    public void btnReplace_OnAction(ActionEvent actionEvent) {
    }

    public void btnReplaceAll_OnAction(ActionEvent actionEvent) {
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