package com.notnotme.jfxparser.controller.ui;

import com.notnotme.jfxparser.controller.factory.ControllerFactory;
import com.notnotme.jfxparser.controller.factory.StageController;
import com.notnotme.jfxparser.controller.processor.ParserFileType;
import com.notnotme.jfxparser.ui.view.EditorTab;
import com.notnotme.jfxparser.utils.Utils;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MainWindowController extends StageController {

    private final static String TAG = MainWindowController.class.getSimpleName();

    @FXML
    private BorderPane mRoot;

    @FXML
    private Menu mMenuNew;

    @FXML
    private MenuItem mItemLoad;

    @FXML
    private MenuItem mItemClose;

    @FXML
    private MenuItem mItemAbout;

    @FXML
    private TabPane mTabPane;

    @FXML
    private Label mStatusLabel;

    @FXML
    private ComboBox<ParserFileType> mParserChooser;

    private final ArrayList<EditorTabController> mTabControllers;
    private FileChooser mFileChooser;

    private final ChangeListener<ParserFileType> mParserChooserListener = new ChangeListener<ParserFileType>() {
        @Override
        public void changed(ObservableValue<? extends ParserFileType> observable, ParserFileType oldValue, ParserFileType newValue) {
            if (newValue == null || mTabPane.getTabs().isEmpty()) return;
            mTabControllers.get(mTabPane.getSelectionModel().getSelectedIndex())
                    .setParserFileType(newValue);
        }
    };

    public static void create(Application application, Stage stage) throws Exception {
        FXMLLoader.load(MainWindowController.class.getResource("/com/notnotme/jfxparser/ui/fxml/MainWindow.fxml"),
                ResourceBundle.getBundle("com.notnotme.jfxparser.ui.fxml.ui"),
                null,
                new ControllerFactory(application, stage));
    }

    public MainWindowController(Application application, Stage stage) {
        super(application, stage);
        mTabControllers = new ArrayList<>();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);

        // Build a list of menuItem for the supported files
        for (ParserFileType parserFileType : ParserFileType.values()) {
            MenuItem menuItem = new MenuItem(parserFileType.getName());
            menuItem.setOnAction((ActionEvent event) -> createNewEditorTab(parserFileType,
                    new File(getResources().getString("new_file") + " " + mTabControllers.size()), false));

            mMenuNew.getItems().add(menuItem);
        }

        mItemLoad.setOnAction(event -> {
            loadFile(null);
            event.consume();
        });
        mItemClose.setOnAction(event -> {
            onStageClose();
            event.consume();
        });
        mItemAbout.setOnAction(event -> {
            showAboutDialog();
            event.consume();
        });

        mTabPane.setOnDragOver(event -> {
            if (event.getGestureSource() != mTabPane && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });
        mTabPane.setOnDragEntered(event -> {
            if (event.getGestureSource() != mTabPane && event.getDragboard().hasFiles()) {
                mTabPane.setOpacity(0.7f);
                mTabPane.setBackground(new Background(
                        new BackgroundFill(Color.LIGHTGREY, CornerRadii.EMPTY, Insets.EMPTY)));
            }
            event.consume();
        });
        mTabPane.setOnDragExited(event -> {
            if (event.getGestureSource() != mTabPane && event.getDragboard().hasFiles()) {
                mTabPane.setOpacity(1f);
                mTabPane.setBackground(Background.EMPTY);
            }
            event.consume();
        });
        mTabPane.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                db.getFiles().forEach((file) -> {
                    ParserFileType type = ParserFileType.getTypeByExtension(file);
                    createNewEditorTab(type, file, true);
                });
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });

        mParserChooser.getItems().addAll(ParserFileType.values());
        mParserChooser.valueProperty().addListener(mParserChooserListener);
        mParserChooser.setDisable(true);
        mParserChooser.setConverter(new StringConverter<ParserFileType>() {
            @Override
            public String toString(ParserFileType object) {
                return object == null ? "" : object.getName();
            }

            @Override
            public ParserFileType fromString(String string) {
                return ParserFileType.getTypeByName(string);
            }
        });


        mFileChooser = new FileChooser();
        mFileChooser.setInitialFileName("");
        mFileChooser.getExtensionFilters().addAll(ParserFileType.getExtensionFilters());

        Stage stage = getStage();
        stage.setTitle(resources.getString("appname"));
        stage.setScene(new Scene(mRoot));
        stage.centerOnScreen();
        stage.setResizable(true);
        stage.setOnCloseRequest(event -> {
            onStageClose();
            event.consume();
        });
        stage.show();

        List<String> params = getApplication().getParameters().getRaw();
        for (String file : params) {
            File diskFile = new File(file);
            if (diskFile.exists()) {
                loadFile(diskFile);
            }
        }
    }

    private void loadFile(File file) {
        ResourceBundle resourceBundle = getResources();

        if (file == null) {
            mFileChooser.setTitle(resourceBundle.getString("load_title"));
            file = mFileChooser.showOpenDialog(getStage());
            if (file != null) {
                mFileChooser.setInitialDirectory(file.getParentFile());
            }
        }

        if (file != null) {
            // We load file according to their extensions, not the content
            ParserFileType type = ParserFileType.getTypeByExtension(file);
            createNewEditorTab(type, file, true);
        }
    }

    private void createNewEditorTab(ParserFileType type, File file, boolean loadFile) {
        if (type == null) {
            Utils.showErrorDialog(file.getName(), getResources().getString("cant_handle_filetype"));
            return;
        }

        try {
            EditorTabController newTabController =
                    EditorTabController.create(getApplication(), getStage(), type);

            EditorTab newTab = new EditorTab(file);
            newTab.setClosable(true);
            newTab.setContent(newTabController.getRoot());
            newTab.setOnCloseRequest(event -> {
                // todo: show yes/no save dialog
                if (newTabController.isEdited()) {
                    newTabController.saveContent();
                }
                mTabControllers.remove(newTabController);
                newTabController.shutDown();
                if (mTabPane.getTabs().size() == 1) {
                    mParserChooser.getSelectionModel().select(null);
                    mParserChooser.setDisable(true);
                    mStatusLabel.setText("");
                }
            });
            newTab.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    EditorTabController tabController = mTabControllers.get(mTabPane.getTabs().indexOf(newTab));
                    tabController.onEditorTabSelected();

                    mParserChooser.valueProperty().removeListener(mParserChooserListener);
                    mParserChooser.getSelectionModel().select(tabController.getParserFileType());
                    mParserChooser.valueProperty().addListener(mParserChooserListener);
                }
            });

            newTabController.setEditorPane(newTab);
            newTabController.setStatusLabel(mStatusLabel);
            mTabControllers.add(newTabController);
            mTabPane.getTabs().add(newTab);
            if (loadFile) {
                newTabController.loadContent();
            }
            mParserChooser.setDisable(false);
            mTabPane.getSelectionModel().select(newTab);
        } catch (Exception ex) {
            Logger.getLogger(TAG).log(Level.SEVERE, null, ex);
            if (ex.getCause() != null) {
                Utils.showErrorDialog(null, ex.getCause().getLocalizedMessage());
            } else {
                Utils.showErrorDialog(null, ex.getLocalizedMessage());
            }
        }
    }

    private void onStageClose() {
        // check if tabs are edited and build a list with them
        mTabControllers.forEach(tabController -> {
            if (tabController.isEdited()) {
                // todo: show yes/no save dialog
                tabController.saveContent();
            }
            tabController.shutDown();
        });

        getStage().hide();
    }

    private void showAboutDialog() {
        try {
            AboutDialogController.create(getApplication(), getStage());
        } catch (Exception ex) {
            Logger.getLogger(TAG).log(Level.SEVERE, null, ex);
            if (ex.getCause() != null) {
                Utils.showErrorDialog(null, ex.getCause().getLocalizedMessage());
            } else {
                Utils.showErrorDialog(null, ex.getLocalizedMessage());
            }
        }
    }

}
