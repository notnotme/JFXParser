package com.notnotme.jfxparser.controller.ui;

import com.notnotme.jfxparser.controller.factory.ControllerFactory;
import com.notnotme.jfxparser.controller.factory.StageController;
import com.notnotme.jfxparser.controller.processor.Parser;
import com.notnotme.jfxparser.controller.processor.ParserFileType;
import com.notnotme.jfxparser.ui.view.EditorTab;
import com.notnotme.jfxparser.ui.view.EditorTreeTableRow;
import com.notnotme.jfxparser.utils.Utils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;

import java.io.BufferedReader;
import java.io.File;
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

public final class EditorTabController extends StageController {

    private final static String TAG = EditorTabController.class.getSimpleName();

    @FXML
    private BorderPane mRoot;

    @FXML
    private CodeArea mCodeArea;

    @FXML
    private TreeTableView<Pair<String, ?>> mTreeTableView;

    private ContextMenu mEditorContextMenu;

    private String mStatusMessage;
    private Paint mStatusColor;
    private Label mStatusLabel;

    private ParserFileType mParserFileType;
    private EditorTab mEditorTab;
    private Parser mParser;

    private final ExecutorService mExecutorService;
    private Future mParsingFuture;

    private final ChangeListener<String> mCodeAreaChangeListener = (obs, oldText, newText) -> parseCode(newText);

    static EditorTabController create(Application application, Stage stage, ParserFileType type) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                EditorTab.class.getResource("/com/notnotme/jfxparser/ui/fxml/EditorTab.fxml"),
                ResourceBundle.getBundle("com.notnotme.jfxparser.ui.fxml.ui"),
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

        // It's a hack... We need to embed the CodeArea in a VirtualizedScrollPane to make it show scrollbars...
        // But this scroll pane is not usable in XML and so the EditorTab.xml file is wrong as the wrap should be done inside it.
        ((SplitPane) mRoot.getChildren().get(0)).getItems().remove(mCodeArea);
        ((SplitPane) mRoot.getChildren().get(0)).getItems().add(0, new VirtualizedScrollPane<>(mCodeArea));

        mCodeArea.setParagraphGraphicFactory(LineNumberFactory.get(mCodeArea));
        mCodeArea.textProperty().addListener(mCodeAreaChangeListener);
        mCodeArea.setOnContextMenuRequested(event -> {
            mCodeArea.getContextMenu().show(getStage());
            event.consume();
        });

        mTreeTableView.setRowFactory(param -> new EditorTreeTableRow(getResources()));
        setParserFileType(mParserFileType);
    }

    void shutDown() {
        try {
            mExecutorService.shutdown();
            mExecutorService.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Logger.getLogger(TAG).log(Level.SEVERE, null, ex);
        }
    }

    void setParserFileType(ParserFileType parserFileType) {
        mParserFileType = parserFileType;
        mParser = parserFileType.createParser();

        mCodeArea.getStylesheets().clear();
        mTreeTableView.getColumns().clear();
        mTreeTableView.setRoot(null);

        mCodeArea.getStylesheets().add(mParser.getStylesheets());
        mTreeTableView.getColumns().addAll(mParser.getTreeTableViewColumns());

        parseCode(mCodeArea.getText());
    }

    void onEditorTabSelected() {
        // todo: later we can use an other kind of control to show character count
        // or caret position, number of words..
        setStatusMessage(mStatusMessage, mStatusColor);

        // Without this there is a bug when you open another tab then switch to a previous one.
        // Without this trying to format text (ctrl+space) will always format the last tab.
        mCodeArea.setContextMenu(null);
        mCodeArea.setContextMenu(mEditorContextMenu);
        Platform.runLater(() -> mCodeArea.requestFocus());
    }

    void loadContent() {
        String line;
        StringBuilder stringBuilder = new StringBuilder();

        File file = getEditorTab().getFile();
        String lineSeparator = String.format("%n");
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append(lineSeparator);
            }
        } catch (IOException ex) {
            Logger.getLogger(TAG).log(Level.SEVERE, null, ex);
        }

        try {
            String code = mParser.prettyPrint(stringBuilder.toString());
            mCodeArea.replaceText(code);
        } catch (Exception e) {
            Utils.showErrorDialog(null, e.getLocalizedMessage());
        }
    }

    private ContextMenu createContextMenu() {
        ResourceBundle resources = getResources();
        ContextMenu menu = new ContextMenu();

        MenuItem itemCopy = new MenuItem(resources.getString("copy"));
        itemCopy.setAccelerator(KeyCombination.keyCombination("CTRL+C"));
        itemCopy.setOnAction(event -> {
            mCodeArea.copy();
            event.consume();
        });

        MenuItem itemCut = new MenuItem(resources.getString("cut"));
        itemCut.setAccelerator(KeyCombination.keyCombination("CTRL+X"));
        itemCut.setOnAction(event -> {
            mCodeArea.cut();
            event.consume();
        });

        MenuItem itemPaste = new MenuItem(resources.getString("paste"));
        itemPaste.setAccelerator(KeyCombination.keyCombination("CTRL+V"));
        itemPaste.setOnAction(event -> {
            mCodeArea.paste();
            event.consume();
        });

        MenuItem itemPrettyPrint = new MenuItem(resources.getString("format"));
        itemPrettyPrint.setAccelerator(KeyCombination.keyCombination("CTRL+SPACE"));
        itemPrettyPrint.setOnAction(event -> {
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
        if (mParsingFuture != null && !mParsingFuture.isDone()) {
            mParsingFuture.cancel(true);
        }

        mParsingFuture = mExecutorService.submit(() -> {
            final StyleSpans<? extends Collection<String>> spans = mParser.computeHighlighting(code);
            Platform.runLater(() -> {
                try {
                    mCodeArea.setStyleSpans(0, spans);
                } catch (Exception ignored) {
                }
            });

            TreeItem<Pair<String, ?>> rootTreeItem;
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

    private EditorTab getEditorTab() {
        return mEditorTab;
    }

    ParserFileType getParserFileType() {
        return mParserFileType;
    }

    BorderPane getRoot() {
        return mRoot;
    }

    boolean isEdited() {
        Logger.getLogger(TAG).log(Level.INFO, "Not implemented");
        return false;
    }

    void saveContent() {
        Logger.getLogger(TAG).log(Level.INFO, "Not implemented: {0}", getEditorTab().getFile());
    }

    void setEditorPane(EditorTab editorTab) {
        mEditorTab = editorTab;
    }

    void setStatusLabel(Label label) {
        mStatusLabel = label;
    }

}
