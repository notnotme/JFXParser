package com.notnotme.jsparser.controller.processor.json;

import com.eclipsesource.json.JsonValue;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.util.Callback;
import javafx.util.Pair;

class TypeCellFactory implements Callback<TreeTableColumn<Pair<String, JsonValue>, String>, TreeTableCell<Pair<String, JsonValue>, String>> {

	public TypeCellFactory() {
	}

	@Override
	public TreeTableCell<Pair<String, JsonValue>, String> call(TreeTableColumn<Pair<String, JsonValue>, String> param) {
		return new TextFieldTreeTableCell<> ();
	}
	
}
