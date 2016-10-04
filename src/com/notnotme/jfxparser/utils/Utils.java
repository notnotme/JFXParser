package com.notnotme.jfxparser.utils;

import javafx.scene.control.Alert;

import java.io.File;

public final class Utils {

	private final static String TAG = Utils.class.getSimpleName();

	public static void showErrorDialog(String header, String content) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setHeaderText(header);
		alert.setContentText(content);
		alert.showAndWait();
	}

	public static String getExtension(File file) {
		String name = file.getName();
		int i = file.getName().lastIndexOf('.');
		if (i >= 0 && name.length() >= i+1) {
			return file.getName().substring(i+1).toLowerCase();
		} else {
			return "";
		}
	}

}
