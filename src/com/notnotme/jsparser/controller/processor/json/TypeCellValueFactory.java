package com.notnotme.jsparser.controller.processor.json;

import com.eclipsesource.json.JsonValue;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeTableColumn;
import javafx.util.Callback;
import javafx.util.Pair;

class TypeCellValueFactory implements Callback<TreeTableColumn.CellDataFeatures<Pair<String, JsonValue>, String>, ObservableValue<String>> {

	public TypeCellValueFactory() {
	}

	@Override
	public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<Pair<String, JsonValue>, String> param) {
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
	}
	
}
