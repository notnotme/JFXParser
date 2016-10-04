package com.notnotme.jfxparser.controller.processor;

import com.notnotme.jfxparser.controller.processor.json.JsonParser;
import com.notnotme.jfxparser.utils.Utils;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public enum ParserFileType {
	JSON("Json", JsonParser.class, "json", "js");

	private final String mName;
	private final Class<? extends Parser> mClass;
	private final String[] mExtensions;

	ParserFileType(String name, Class<? extends Parser> clazz, String... extensions) {
		mName = name;
		mClass = clazz;
		mExtensions = extensions;
	}

	public String getName() {
		return mName;
	}

	public String[] getExtensions() {
		return mExtensions;
	}

	public Parser createParser() {
		try {
			return mClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public static ParserFileType getTypeByExtension(File file) {
		String extension = Utils.getExtension(file);
		for(ParserFileType type : values()) {
			for (String ext : type.mExtensions) {
				if (ext.equals(extension)) {
					return type;
				}
			}
		}
		return null;
	}

	public static ParserFileType getTypeByName(String name) {
		for(ParserFileType type : values()) {
			if (type.mName.equals(name)) {
				return type;
			}
		}
		return null;
	}

	public static List<FileChooser.ExtensionFilter> getExtensionFilters() {
		ArrayList<FileChooser.ExtensionFilter> filters = new ArrayList<>();
		ArrayList<String> extensionsList = new ArrayList<>();

		for (ParserFileType type : values()) {
			for (String extension : type.mExtensions) {
				extensionsList.add("*." + extension);
			}

			filters.add(new FileChooser.ExtensionFilter(type.mName, extensionsList));
			extensionsList.clear();
		}

		return filters;
	}

	public static Parser createParser(ParserFileType type) {
		return type.createParser();
	}

}
