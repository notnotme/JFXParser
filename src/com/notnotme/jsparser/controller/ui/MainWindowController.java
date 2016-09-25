package com.notnotme.jsparser.controller.ui;

import com.notnotme.jsparser.controller.factory.ControllerFactory;
import com.notnotme.jsparser.controller.factory.StageController;
import com.notnotme.jsparser.controller.processor.ParserFileType;
import com.notnotme.jsparser.ui.view.EditorTab;
import com.notnotme.jsparser.utils.Utils;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.StringConverter;

public final class MainWindowController extends StageController {

	private final static String TAG = MainWindowController.class.getSimpleName();

	@FXML private BorderPane mRoot;
	@FXML private Menu mMenuNew;
	@FXML private MenuItem mItemLoad;
	@FXML private MenuItem mItemClose;
	@FXML private MenuItem mItemAbout;
	@FXML private TabPane mTabPane;
	@FXML private Label mStatusLabel;
	@FXML private ComboBox<ParserFileType> mParserChooser;

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
		FXMLLoader.load(MainWindowController.class.getResource("/com/notnotme/jsparser/ui/fxml/MainWindow.fxml"),
                ResourceBundle.getBundle("com.notnotme.jsparser.ui.fxml.ui"),
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
		for(ParserFileType parserFileType : ParserFileType.values()) {
			MenuItem menuItem = new MenuItem(parserFileType.getName());
			menuItem.setOnAction((ActionEvent event) -> {
				createNewEditorTab(parserFileType,
						new File(getResources().getString("new_file") + " " + mTabControllers.size()), false);
			});
			mMenuNew.getItems().add(menuItem);
		}
		mItemLoad.setOnAction((ActionEvent event) -> {
			loadFile();
			event.consume();
		});
		mItemClose.setOnAction((ActionEvent event) -> {
			onStageClose();
			event.consume();
		});
		mItemAbout.setOnAction((ActionEvent event) -> {
			showAboutDialog();
			event.consume();
		});

		mTabPane.setOnDragOver((DragEvent event) -> {
			if (event.getGestureSource() != mTabPane && event.getDragboard().hasFiles()) {
				event.acceptTransferModes(TransferMode.COPY);
			}
			event.consume();
		});
		mTabPane.setOnDragEntered((DragEvent event) -> {
			if (event.getGestureSource() != mTabPane && event.getDragboard().hasFiles()) {
				mTabPane.setOpacity(0.7f);
				mTabPane.setBackground(new Background(
						new BackgroundFill(Color.LIGHTGREY, CornerRadii.EMPTY, Insets.EMPTY)));
			}
			event.consume();
		});
		mTabPane.setOnDragExited((DragEvent event) -> {
			if (event.getGestureSource() != mTabPane && event.getDragboard().hasFiles()) {
				mTabPane.setOpacity(1f);
				mTabPane.setBackground(Background.EMPTY);
			}
			event.consume();
		});
		mTabPane.setOnDragDropped((DragEvent event) -> {
			Dragboard db = event.getDragboard();
			boolean success = false;
			if (db.hasFiles()) {
				db.getFiles().stream().forEach((file) -> {
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
				return object.getName();
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
		stage.setOnCloseRequest((WindowEvent event) -> {
			onStageClose();
			event.consume();
		});
		stage.show();
	}

	private void loadFile() {
		ResourceBundle resourceBundle = getResources();
		mFileChooser.setTitle(resourceBundle.getString("load_title"));
		File file = mFileChooser.showOpenDialog(getStage());
		if (file != null) {
			mFileChooser.setTitle(resourceBundle.getString("load"));
			mFileChooser.setInitialDirectory(file.getParentFile());
			
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
			newTab.setOnCloseRequest((Event event) -> {
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
				event.consume();
			});
			newTab.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
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
		mTabControllers.stream().forEach((tabController) -> {
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
