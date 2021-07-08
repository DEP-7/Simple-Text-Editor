package controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;

public class AboutFormController {
    public AnchorPane pneAbout;
    public Label lblLetter1;
    public Label lblLetter2;
    public Label lblLetter3;
    public Label lblLetter4;
    public Label lblLetter5;
    public Label lblLetter6;
    public Label lblLetter7;
    public Label lblLetter8;
    public Label lblLetter9;
    public Label lblLetter10;
    public Label lblLetter11;
    public Label lblLetter12;
    public Label lblLetter13;
    public Label lblLetter14;
    public Label lblLetter15;
    public Label lblLetter16;
    private  int degrees = 0;
    private Timeline timeline;

    public void initialize() {
        animateText();

        Platform.runLater(() -> {
            lblLetter1.getScene().getWindow().setOnCloseRequest(event -> {
                timeline.stop();
            });
        });
    }

    private void animateText() {
        pneAbout.layoutBoundsProperty().addListener(new ChangeListener<Bounds>() {
            @Override
            public void changed(ObservableValue<? extends Bounds> observable, Bounds oldValue, Bounds newValue) {
                timeline = new Timeline(new KeyFrame(Duration.millis(50), event -> {
                    lblLetter1.setTranslateY(-10 * Math.sin((degrees) * Math.PI / 180));
                    lblLetter2.setTranslateY(-10 * Math.sin((degrees + 180 * 1 / 15) * Math.PI / 180));
                    lblLetter3.setTranslateY(-10 * Math.sin((degrees + 180 * 2 / 15) * Math.PI / 180));
                    lblLetter4.setTranslateY(-10 * Math.sin((degrees + 180 * 3 / 15) * Math.PI / 180));
                    lblLetter5.setTranslateY(-10 * Math.sin((degrees + 180 * 4 / 15) * Math.PI / 180));
                    lblLetter6.setTranslateY(-10 * Math.sin((degrees + 180 * 5 / 15) * Math.PI / 180));
                    lblLetter7.setTranslateY(-10 * Math.sin((degrees + 180 * 6 / 15) * Math.PI / 180));
                    lblLetter8.setTranslateY(-10 * Math.sin((degrees + 180 * 7 / 15) * Math.PI / 180));
                    lblLetter9.setTranslateY(-10 * Math.sin((degrees + 180 * 8 / 15) * Math.PI / 180));
                    lblLetter10.setTranslateY(-10 * Math.sin((degrees + 180 * 9 / 15) * Math.PI / 180));
                    lblLetter11.setTranslateY(-10 * Math.sin((degrees + 180 * 10 / 15) * Math.PI / 180));
                    lblLetter12.setTranslateY(-10 * Math.sin((degrees + 180 * 11 / 15) * Math.PI / 180));
                    lblLetter13.setTranslateY(-10 * Math.sin((degrees + 180 * 12 / 15) * Math.PI / 180));
                    lblLetter14.setTranslateY(-10 * Math.sin((degrees + 180 * 13 / 15) * Math.PI / 180));
                    lblLetter15.setTranslateY(-10 * Math.sin((degrees + 180 * 14 / 15) * Math.PI / 180));
                    lblLetter16.setTranslateY(-10 * Math.sin((degrees + 180 * 15 / 15) * Math.PI / 180));
                    degrees += 1;

                    if (degrees > 720) {
                        degrees = 0;
                    }
                }));
                timeline.setCycleCount(Animation.INDEFINITE);
                timeline.setRate(3);
                timeline.playFromStart();
            }
        });
    }
}
