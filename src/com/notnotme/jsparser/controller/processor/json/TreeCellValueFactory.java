package com.notnotme.jsparser.controller.processor.json;

import com.eclipsesource.json.JsonValue;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeTableColumn;
import javafx.util.Callback;
import javafx.util.Pair;

class TreeCellValueFactory implements Callback<TreeTableColumn.CellDataFeatures<Pair<String, JsonValue>, String>, ObservableValue<String>> {

	public TreeCellValueFactory() {
	}

	@Override
	public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<Pair<String, JsonValue>, String> param) {
		return new ReadOnlyStringWrapper(param.getValue().getValue().getKey());
	}
	
}
