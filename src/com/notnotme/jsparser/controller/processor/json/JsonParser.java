package com.notnotme.jsparser.controller.processor.json;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.eclipsesource.json.WriterConfig;
import com.notnotme.jsparser.controller.processor.Parser;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.util.Pair;
import org.fxmisc.richtext.StyleSpans;
import org.fxmisc.richtext.StyleSpansBuilder;

public final class JsonParser implements Parser<Pair<String, JsonValue>, String> {

	private final static String TAG = JsonParser.class.getSimpleName();

	private static final String BRACE_PATTERN = "(\\{|\\})";
	private static final String BRACKET_PATTERN = "(\\[|\\])";

	private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
	private static final String NUMBER_PATTERN = "([0-9.]+)";
	private static final String BOOLEAN_PATTERN = "(?i:true|false)";
	private static final String NULL_PATTERN = "(?i:null)";

	private static final Pattern PATTERN = Pattern.compile(
			"(?<BRACE>" + BRACE_PATTERN + ")"
			+ "|(?<BRACKET>" + BRACKET_PATTERN + ")"
			+ "|(?<STRING>" + STRING_PATTERN + ")"
			+ "|(?<NUMBER>" + NUMBER_PATTERN + ")"
			+ "|(?<NULL>" + NULL_PATTERN + ")"
			+ "|(?<BOOLEAN>" + BOOLEAN_PATTERN + ")"
	);

	private JsonValue mJsonValue;
	
	public JsonParser() {
	}

	@Override
	public String getStylesheets() {
		return getClass().getResource("/com/notnotme/jsparser/controller/processor/json/json.css").toExternalForm();
	}

	@Override
	public String prettyPrint(String code) throws Exception {
		StringWriter writer = new StringWriter();
		Json.parse(code).writeTo(writer, WriterConfig.PRETTY_PRINT);
		return writer.toString();
	}

	@Override
	public StyleSpans<Collection<String>> computeHighlighting(String code) {
		Matcher matcher;
		try {
			matcher = PATTERN.matcher(code);
		} catch (Exception e) {
			return null;
		}
		
		int lastKwEnd = 0;
		StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
		while (matcher.find()) {
			String styleClass
					= matcher.group("BRACE") != null ? "brace"
					: matcher.group("BRACKET") != null ? "bracket"
					: matcher.group("STRING") != null ? "string"
					: matcher.group("NUMBER") != null ? "number"
					: matcher.group("NULL") != null ? "null"
					: matcher.group("BOOLEAN") != null ? "boolean"
					: null;

			spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
			spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
			lastKwEnd = matcher.end();
		}
		spansBuilder.add(Collections.emptyList(), code.length() - lastKwEnd);
		return spansBuilder.create();
	}

	@Override
	public List<TreeTableColumn<Pair<String, JsonValue>, String>> getTreeTableViewColumns() {
		TreeTableColumn<Pair<String, JsonValue>, String> columnTree = new TreeTableColumn<>("TREE");
		columnTree.setPrefWidth(150);
		columnTree.setCellValueFactory(new TreeCellValueFactory());
		columnTree.setCellFactory(new TreeCellFactory());

		TreeTableColumn<Pair<String, JsonValue>, String> columnType = new TreeTableColumn<>("TYPE");
		columnType.setPrefWidth(150);
		columnType.setCellValueFactory(new TypeCellValueFactory());
		columnType.setCellFactory(new TypeCellFactory());

		TreeTableColumn<Pair<String, JsonValue>, String> columnValue = new TreeTableColumn<>("VALUE");
		columnValue.setPrefWidth(250);
		columnValue.setCellValueFactory(new ValueCellValueFactory());
		columnValue.setCellFactory(new ValueCellFactory());

		ArrayList<TreeTableColumn<Pair<String, JsonValue>, String>> columnList = new ArrayList<>();
		columnList.add(columnTree);
		columnList.add(columnType);
		columnList.add(columnValue);
		
		return columnList;
	}
	
	@Override
	public TreeItem<Pair<String, JsonValue>> parseCode(String code) throws Exception {
		TreeItem<Pair<String, JsonValue>> rootItem = new TreeItem<>(new Pair<>("Root", Json.parse(code)));
		rootItem.getChildren().addAll(parse(rootItem.getValue().getValue()));
		rootItem.setExpanded(true);
		return rootItem;
	}
	
	private List<TreeItem<Pair<String, JsonValue>>> parse(JsonValue jsonValue) {
		List<TreeItem<Pair<String, JsonValue>>> childs = new ArrayList<>();
		if (jsonValue.isObject()) {
			for (JsonObject.Member member : jsonValue.asObject()) {
				TreeItem<Pair<String, JsonValue>> child = new TreeItem<>(
						new Pair<>(member.getName(), member.getValue()));
				
				child.getChildren().addAll(parse(child.getValue().getValue()));
				child.setExpanded(true);
				childs.add(child);
			}
		} else if (jsonValue.isArray()) {
			int index = 0;
			for (JsonValue value : jsonValue.asArray()) {
				TreeItem<Pair<String, JsonValue>> child = new TreeItem<>(
						new Pair<>("[" + index + "]", value));
				
				child.getChildren().addAll(parse(value));
				child.setExpanded(true);
				childs.add(child);
				index++;
			}
		}
		
		return childs;
	}

}
