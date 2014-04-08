/*
 * Copyright 2014 Alejandro Barocio A. <abarocio80@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.extranjero.web;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.time.LocalDateTime;

/**
 * Created by alex on 3/31/14.
 */
public class Browser extends Application {

    private final Image previousI = new Image(Browser.class.getResourceAsStream("img/back.png"));
    private final ImageView previousIV = new ImageView(previousI);
    private final Image nextI = new Image(Browser.class.getResourceAsStream("img/next.png"));
    private final ImageView nextIV = new ImageView(nextI);
    private final Image reloadI = new Image(Browser.class.getResourceAsStream("img/reload.png"));
    private final ImageView reloadIV = new ImageView(reloadI);
    private final Image cancelI = new Image(Browser.class.getResourceAsStream("img/cancel.png"));
    private final ImageView cancelIV = new ImageView(cancelI);
    private final Image settingsI = new Image(Browser.class.getResourceAsStream("img/settings.png"));
    private final ImageView settingsIV = new ImageView(settingsI);
    @FXML
    ProgressIndicator pi = new ProgressIndicator();

    @FXML
    ProgressBar pb = new ProgressBar();
    private Stage mStage;
    private Scene mScene;
    @FXML
    private StackPane progress;
    @FXML
    private TextField uri;
    @FXML
    private Button title;
    @FXML
    private Text pt;
    @FXML
    private Text pu;
    @FXML
    private WebEngine webEngine;
    @FXML
    private Text url;
    @FXML
    private TextField addressbar;
    @FXML
    private Button reload;
    @FXML
    private Button previous;
    @FXML
    private Button next;
    @FXML
    private Menu settings;
    @FXML
    private WebView mWebView;
    private boolean loggedLoad;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = (Parent) FXMLLoader.load(getClass().getResource("ui/BrowserUI.fxml"));
        stage.setTitle("Web.Browser");
        mStage = stage;
        mScene = new Scene(root, 800, 600);
        mStage.setScene(mScene);
        mScene.getStylesheets().add(getClass().getResource("skins/light.css").toExternalForm());
        mStage.show();

        ChangeListener<Number> widthListener = new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                title.setPrefWidth(uri.getWidth());
                pb.setPrefWidth(uri.getWidth());
                progress.setPrefWidth(uri.getWidth());
                mWebView.setPrefWidth(mStage.getWidth());
                mWebView.setPrefHeight(mStage.getHeight());
            }
        };

        /*
        mStage.widthProperty().addListener(widthListener);
        mStage.heightProperty().addListener(widthListener);

        mScene.widthProperty().addListener(widthListener);
        mScene.heightProperty().addListener(widthListener);
        */

    }

    @FXML
    void reload(ActionEvent event) {
        if (webEngine.getLoadWorker().isRunning()) {
            webEngine.getLoadWorker().cancel();
            System.out.println(LocalDateTime.now() + " CancelLoading: " + webEngine.getLocation());
            reload.setGraphic(reloadIV);
            uri.setText(webEngine.getLocation());
            title.setText(webEngine.getTitle());
            title.setPrefWidth(uri.getWidth());
            uri.setVisible(false);
            progress.setVisible(false);
            title.setVisible(true);
        } else {
            go(event);
        }
    }

    @FXML
    void go(ActionEvent event) {

        reload.setGraphic(cancelIV);
        //System.out.println(event.toString());
        String url = uri.getText();

        if (url.matches("^https?://") || url.startsWith("http://") || url.startsWith("https://")) {
            //System.out.println("uri OK");
        } else {
            //System.out.println("uri normalization");
            url = "http://" + url;
        }

        uri.setVisible(false);
        progress.setPrefWidth(uri.getWidth());
        progress.setVisible(true);

        webEngine.load(url);
        System.out.println(LocalDateTime.now() + " StartLoading: " + webEngine.getLocation());
        loggedLoad = false;

        webEngine.getLoadWorker().stateProperty().addListener(
                new ChangeListener<Worker.State>() {
                    public void changed(ObservableValue<? extends Worker.State> arg0,
                                        Worker.State arg1, Worker.State arg2) {

                    }
                }
        );

        webEngine.getLoadWorker().progressProperty().addListener(new ChangeListener<Object>() {

            @Override
            public void changed(ObservableValue<?> arg0, Object arg1, Object arg2) {
                progress.setVisible(true);
                double p = webEngine.getLoadWorker().progressProperty().get();
                String l = webEngine.getLoadWorker().getMessage();
                pb.setPrefWidth(uri.getWidth());

                if (p < 1.0) {
                    pt.setText((int) (p * 100) + "%");
                    pu.setText(l);
                } else if (p > 0.0) {
                    if (!loggedLoad) {
                        System.out.println(LocalDateTime.now() + " DoneLoading: " + webEngine.getLocation());
                        loggedLoad = true;
                    }
                    title.setPrefWidth(uri.getWidth());
                    title.setText(webEngine.getTitle());
                    progress.setVisible(false);
                    reload.setGraphic(reloadIV);
                    title.setVisible(true);
                }
                uri.setText(webEngine.getLocation());
                pi.setProgress((Double) arg0.getValue());
                pb.setProgress((Double) arg0.getValue());
            }
        });
        webEngine.locationProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                uri.setText(newValue);
            }
        });

        uri.setText(webEngine.getLocation());
    }

    @FXML
    void toggle(ActionEvent event) {
        if (title.isVisible()) {
            title.setVisible(false);
            uri.setVisible(true);
            uri.requestFocus();
            uri.selectAll();
        } else {
            uri.setVisible(false);
            title.setVisible(true);
        }
    }

    @FXML
    void next(ActionEvent event) {
        webEngine.executeScript("history.forward()");
    }

    @FXML
    void previous(ActionEvent event) {
        webEngine.executeScript("history.back()");
    }


    @FXML
    void initialize() {

        webEngine = mWebView.getEngine();
        webEngine.setOnResized(new EventHandler<WebEvent<Rectangle2D>>() {
            @Override
            public void handle(WebEvent<Rectangle2D> rectangle2DWebEvent) {
                title.setPrefWidth(uri.getWidth());
                progress.setPrefWidth(uri.getWidth());
                pb.setPrefWidth(uri.getWidth());
                pb.setMaxWidth(uri.getWidth());
                title.setMaxWidth(uri.getWidth());
            }
        });

        double size = 16.0;

        previousIV.setFitHeight(size);
        previousIV.setFitWidth(size);
        previous.setGraphic(previousIV);

        nextIV.setFitHeight(size);
        nextIV.setFitWidth(size);
        next.setGraphic(nextIV);

        reloadIV.setFitHeight(size);
        reloadIV.setFitWidth(size);
        reload.setGraphic(reloadIV);

        settingsIV.setFitWidth(size);
        settingsIV.setFitHeight(size);
        settings.setGraphic(settingsIV);

        cancelIV.setFitHeight(16.0);
        cancelIV.setFitWidth(16.0);
    }
}
