package com.notnotme.jsparser.controller.processor.json;

import com.eclipsesource.json.JsonValue;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeTableColumn;
import javafx.util.Callback;
import javafx.util.Pair;

class ValueCellValueFactory implements Callback<TreeTableColumn.CellDataFeatures<Pair<String, JsonValue>, String>, ObservableValue<String>> {

	@Override
	public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<Pair<String, JsonValue>, String> param) {
		JsonValue value = param.getValue().getValue().getValue();
		if (value.isObject() || value.isArray()) return null;
		
		String stringValue;
		if (value.isString()) {
			stringValue = value.asString();
		} else {
			stringValue = param.getValue().getValue().getValue().toString();
		}
		
		return new ReadOnlyStringWrapper(stringValue);
	}
	
}
