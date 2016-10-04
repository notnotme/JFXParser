package com.notnotme.jfxparser.controller.processor;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import org.fxmisc.richtext.StyleSpans;

import java.util.Collection;
import java.util.List;

public interface Parser<S,T> {

	/**
	 * @return The style sheet containing the color classes usable with
	 * computeHighlighting
	 */
	String getStylesheets();

	/**
	 * Compute the highlight of the current editor's code
	 * @param code The content of the editor as a String
	 * @return  A StyleSpans of Strings containing the css classes to apply to the text
	 */
	StyleSpans<Collection<String>> computeHighlighting(String code);

	/**
	 * Parse a String
	 * @param code The code to parse, as a string
	 * @return The root TreeItem than describe the parsed code
	 * @throws Exception If an error occur during parsing
	 */
	TreeItem<S> parseCode(String code) throws Exception;

	/**
	 * Return a pretty print of code
	 * @return A String that represent the same code as before, but pretty printed
	 * @param code A String representing the code
	 * @throws Exception If an error occur during the operation
	 */
	String prettyPrint(String code) throws Exception;

	/**
	 * @return A list of column to display in the TreeTableView next to the editor
	 */
	List<TreeTableColumn<S, T>> getTreeTableViewColumns();

}