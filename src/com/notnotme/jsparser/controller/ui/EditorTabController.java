package com.notnotme.jsparser.controller.ui;

import com.notnotme.jsparser.controller.factory.ControllerFactory;
import com.notnotme.jsparser.controller.factory.StageController;
import com.notnotme.jsparser.controller.processor.Parser;
import com.notnotme.jsparser.controller.processor.ParserFileType;
import com.notnotme.jsparser.ui.view.EditorTab;
import com.notnotme.jsparser.utils.Utils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.StyleSpans;

public final class EditorTabController extends StageController {
	
	private final static String TAG = EditorTabController.class.getSimpleName();

	@FXML private BorderPane mRoot;
	@FXML private SplitPane mSplitPane;
	@FXML private CodeArea mCodeArea;
	@FXML private TreeTableView mTreeTableView;

	private ContextMenu mEditorContextMenu;
	
	private String mStatusMessage;
	private Paint mStatusColor;
	private Label mStatusLabel;

	private ParserFileType mParserFileType;
	private EditorTab mEditorTab;
	private Parser mParser;
	
	private final ExecutorService mExecutorService;
	private Future mParsingFutur;

	private final ChangeListener<String> mCodeAreaChangeListener = (obs, oldText, newText) -> {
			parseCode(newText);
		};

	public static EditorTabController create(Application application, Stage stage, ParserFileType type) throws Exception {
		FXMLLoader loader = new FXMLLoader(
				EditorTab.class.getResource("/com/notnotme/jsparser/ui/fxml/EditorTab.fxml"),
				ResourceBundle.getBundle("com.notnotme.jsparser.ui.fxml.ui"),
				null,
				new ControllerFactory(application, stage, type));

		loader.load();
		return loader.getController();
	}
	
	public EditorTabController(Application application, Stage stage, ParserFileType type) {
		super(application, stage);
		mParserFileType = type;
		mExecutorService = Executors.newFixedThreadPool(2);
}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		super.initialize(location, resources);
        
		mEditorContextMenu = createContextMenu();
		mCodeArea.setParagraphGraphicFactory(LineNumberFactory.get(mCodeArea));
		mCodeArea.textProperty().addListener(mCodeAreaChangeListener);
		mCodeArea.setOnContextMenuRequested((ContextMenuEvent event) -> {
			mCodeArea.getContextMenu().show(getStage());
			event.consume();
		});
		
		setParserFileType(mParserFileType);
	}
	
	public void shutDown() {
		try {
			mExecutorService.shutdown();
			mExecutorService.awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException ex) {
			Logger.getLogger(TAG).log(Level.SEVERE, null, ex);
		}
	}
	
	public void setParserFileType(ParserFileType parserFileType) {
		mParserFileType = parserFileType;
		mParser = parserFileType.createParser();
				
		mCodeArea.getStylesheets().clear();
		mCodeArea.getStylesheets().add(mParser.getStylesheets());

		mTreeTableView.getColumns().clear();
		mTreeTableView.getColumns().addAll(mParser.getTreeTableViewColumns());

		String code = mCodeArea.getText();
		parseCode(code);
	}

	public void onEditorTabSelected() {
		// todo: later we can use an other kind of control to show character count
		// or carret position, number of words..
		setStatusMessage(mStatusMessage, mStatusColor);
		
		// Without this there is a bug when you open another tab then switch to a previous one.
		// Without this trying to format text (ctrl+space) will always format the last tab.
		mCodeArea.setContextMenu(null);
		mCodeArea.setContextMenu(mEditorContextMenu);
		
		Platform.runLater(() -> {
			mCodeArea.requestFocus();
		});
	}
	
	public void loadContent() {
		String line;
		StringBuilder stringBuilder = new StringBuilder();

		File file = getEditorTab().getFile();
		String lineSeparator = String.format("%n");
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line).append(lineSeparator);
			}
		} catch (FileNotFoundException ex) {
			Logger.getLogger(TAG).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(TAG).log(Level.SEVERE, null, ex);
		}

		mCodeArea.replaceText(stringBuilder.toString());
	}
	
	private ContextMenu createContextMenu() {
		ResourceBundle resources = getResources();
		ContextMenu menu = new ContextMenu();
		
		MenuItem itemCopy = new MenuItem(resources.getString("copy"));
		itemCopy.setAccelerator(KeyCombination.keyCombination("CTRL+C"));
		itemCopy.setOnAction((ActionEvent event) -> {
			mCodeArea.copy();
			event.consume();
		});
		
		MenuItem itemCut = new MenuItem(resources.getString("cut"));
		itemCut.setAccelerator(KeyCombination.keyCombination("CTRL+X"));
		itemCut.setOnAction((ActionEvent event) -> {
			mCodeArea.cut();
			event.consume();
		});

		MenuItem itemPaste = new MenuItem(resources.getString("paste"));
		itemPaste.setAccelerator(KeyCombination.keyCombination("CTRL+V"));
		itemPaste.setOnAction((ActionEvent event) -> {
			mCodeArea.paste();
			event.consume();
		});
		
		MenuItem itemPrettyPrint = new MenuItem(resources.getString("format"));
		itemPrettyPrint.setAccelerator(KeyCombination.keyCombination("CTRL+SPACE"));
		itemPrettyPrint.setOnAction((ActionEvent event) -> {
			String code = mCodeArea.getText();
			try {
				code = mParser.prettyPrint(code);
			} catch (Exception e) {
				Utils.showErrorDialog(null, e.getLocalizedMessage());
			}

			mCodeArea.textProperty().removeListener(mCodeAreaChangeListener);
			mCodeArea.replaceText(code);
			mCodeArea.setStyleSpans(0, mParser.computeHighlighting(code));
			mCodeArea.textProperty().addListener(mCodeAreaChangeListener);
			event.consume();
		});
		
		menu.getItems().addAll(itemCopy, itemCut, itemPaste, new SeparatorMenuItem(), itemPrettyPrint);
		return menu;
	}

	private void parseCode(String code) {
		if (mParsingFutur != null && mParsingFutur.isDone()) {
			mParsingFutur.cancel(true);
		}

		mParsingFutur = mExecutorService.submit(() -> {
			final StyleSpans<Collection<String>> spans = mParser.computeHighlighting(code);
			Platform.runLater(() -> {
				try {
					mCodeArea.setStyleSpans(0, spans);
				} catch (Exception e) {}
			});

			TreeItem rootTreeItem;
			try {
				rootTreeItem = mParser.parseCode(code);
				Platform.runLater(() -> {
					mTreeTableView.setRoot(rootTreeItem);
					setStatusMessage(getResources().getString("ready"), Paint.valueOf("green"));
				});
			} catch (Exception e) {
				Platform.runLater(() -> {
					mTreeTableView.setRoot(null);
					if (code.isEmpty()) {
						setStatusMessage(getResources().getString("ready"), Paint.valueOf("green"));
					} else {
						setStatusMessage(e.getLocalizedMessage(), Paint.valueOf("red"));
					}
				});
			}
		});
	}
	
	private void setStatusMessage(String message, Paint color) {
		mStatusColor = color;
		mStatusMessage = message;
		mStatusLabel.setText(mStatusMessage);
		mStatusLabel.setTextFill(mStatusColor);
	}
	
	public void saveContent() {
		Logger.getLogger(TAG).log(Level.INFO, "Not implemented: {0}", getEditorTab().getFile());
	}

	public boolean isEdited() {
		Logger.getLogger(TAG).log(Level.INFO, "Not implemented");
		return false;
	}
	
	public EditorTab getEditorTab() {
		return mEditorTab;
	}
	
	public void setEditorPane(EditorTab editorTab) {
		mEditorTab = editorTab;
	}

	public ParserFileType getParserFileType() {
		return mParserFileType;
	}

	public BorderPane getRoot() {
		return mRoot;
	}

	public SplitPane getSplitPane() {
		return mSplitPane;
	}

	public CodeArea getCodeArea() {
		return mCodeArea;
	}

	public TreeTableView getTreeTableView() {
		return mTreeTableView;
	}

	public Parser getParser() {
		return mParser;
	}

	public void setStatusLabel(Label label) {
		mStatusLabel = label;
	}
	
	public Label getStatusLabel() {
		return mStatusLabel;
	}
	
}
