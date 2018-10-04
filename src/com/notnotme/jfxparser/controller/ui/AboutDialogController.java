package com.notnotme.jfxparser.controller.ui;

import com.notnotme.jfxparser.controller.factory.ControllerFactory;
import com.notnotme.jfxparser.controller.factory.StageController;
import com.notnotme.jfxparser.utils.ModulePlayer;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class AboutDialogController extends StageController {

    private final static String TAG = AboutDialogController.class.getSimpleName();

    @FXML
    private VBox mRoot;

    @FXML
    private Hyperlink mGithubLink;

    @FXML
    private Hyperlink mSongLink;

    @FXML
    private Hyperlink mIconsLink;

    @FXML
    private Hyperlink mMuxmLink;

    private ModulePlayer mPlayer;

    static void create(Application application, Stage parent) throws Exception {
        Stage stage = new Stage();
        stage.initOwner(parent);
        FXMLLoader.load(AboutDialogController.class.getResource("/com/notnotme/jfxparser/ui/fxml/AboutDialog.fxml"),
                ResourceBundle.getBundle("com.notnotme.jfxparser.ui.fxml.ui"),
                null,
                new ControllerFactory(application, stage));
    }

    public AboutDialogController(Application application, Stage stage) {
        super(application, stage);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);

        mRoot.setOnMouseClicked((MouseEvent event) -> {
            getStage().hide();
            event.consume();
        });
        mGithubLink.setOnAction(event -> {
            getApplication().getHostServices().showDocument(mGithubLink.getText());
            event.consume();
        });
        mMuxmLink.setOnAction(event -> {
            getApplication().getHostServices().showDocument("http://musound.sourceforge.net/muxm/index.html");
            event.consume();
        });
        mSongLink.setOnAction(event -> {
            getApplication().getHostServices().showDocument("http://ultrasyd.free.fr/");
            event.consume();
        });
        mIconsLink.setOnAction(event -> {
            getApplication().getHostServices().showDocument("http://github.com/damieng/silk-companion/");
            event.consume();
        });

        Stage stage = getStage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(resources.getString("about_title"));
        stage.setScene(new Scene(mRoot));
        stage.setResizable(false);
        stage.centerOnScreen();
        stage.setOnShown(event -> {
            onEnter();
            event.consume();
        });
        stage.setOnHiding(event -> {
            onExit();
            event.consume();
        });
        stage.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            getStage().hide();
            event.consume();
        });
        stage.show();
    }

    private void onEnter() {
        try {
            mPlayer = new ModulePlayer(getClass().getResource("/com/notnotme/jfxparser/res/Ultrasyd-Groovy Elisa.xm").toURI().toURL());
            new Thread(mPlayer).start();
        } catch (NullPointerException | IOException | URISyntaxException ex) {
            // can be null if file not found or lib missing
            Logger.getLogger(TAG).log(Level.SEVERE, null, ex);
        }
    }

    private void onExit() {
        if (mPlayer != null) {
            mPlayer.stop();
        }
    }

}
