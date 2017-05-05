package com.notnotme.jfxparser.controller.processor.json;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.eclipsesource.json.WriterConfig;
import com.notnotme.jfxparser.controller.processor.Parser;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.util.Pair;
import org.fxmisc.richtext.StyleSpans;
import org.fxmisc.richtext.StyleSpansBuilder;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class JsonParser implements Parser<Pair<String, JsonValue>, String> {

	private static final String BRACE_PATTERN = "([{}])";
	private static final String BRACKET_PATTERN = "([\\[]])";

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

	public JsonParser() {
	}

	@Override
	public String getStylesheets() {
		return getClass().getResource("/com/notnotme/jfxparser/controller/processor/json/json.css").toExternalForm();
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
		columnTree.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().getKey()));
		columnTree.setCellFactory(param -> new TextFieldTreeTableCell<>());

		TreeTableColumn<Pair<String, JsonValue>, String> columnType = new TreeTableColumn<>("TYPE");
		columnType.setPrefWidth(150);
		columnType.setCellValueFactory(param -> {
			JsonValue value = param.getValue().getValue().getValue();
			String stringValue = "";
			if (value.isObject()) {
				stringValue = "Object";
			} else if (value.isArray()) {
				stringValue = "Array [" + value.asArray().size() + "]";
			} else if (value.isBoolean()) {
				stringValue = "Boolean";
			} else if (value.isString()) {
				stringValue = "String";
			} else if (value.isNumber()) {
				stringValue = "Number";
			} else if (value.isNull()) {
				stringValue = "Object/Array/Value";
			}

			return new ReadOnlyStringWrapper(stringValue);
		});
		columnType.setCellFactory(param -> new TextFieldTreeTableCell<>());

		TreeTableColumn<Pair<String, JsonValue>, String> columnValue = new TreeTableColumn<>("VALUE");
		columnValue.setPrefWidth(250);
		columnValue.setCellValueFactory(param -> {
			JsonValue value = param.getValue().getValue().getValue();
			if (value.isObject() || value.isArray()) return null;

			String stringValue;
			if (value.isString()) {
				stringValue = value.asString();
			} else {
				stringValue = param.getValue().getValue().getValue().toString();
			}

			return new ReadOnlyStringWrapper(stringValue);
		});
		columnValue.setCellFactory(param -> new TextFieldTreeTableCell<>());

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
		List<TreeItem<Pair<String, JsonValue>>> children = new ArrayList<>();
		if (jsonValue.isObject()) {
			for (JsonObject.Member member : jsonValue.asObject()) {
				TreeItem<Pair<String, JsonValue>> child = new TreeItem<>(
						new Pair<>(member.getName(), member.getValue()));

				child.getChildren().addAll(parse(child.getValue().getValue()));
				child.setExpanded(true);
				children.add(child);
			}
		} else if (jsonValue.isArray()) {
			int index = 0;
			for (JsonValue value : jsonValue.asArray()) {
				TreeItem<Pair<String, JsonValue>> child = new TreeItem<>(
						new Pair<>("[" + index + "]", value));

				child.getChildren().addAll(parse(value));
				child.setExpanded(true);
				children.add(child);
				index++;
			}
		}

		return children;
	}

}
