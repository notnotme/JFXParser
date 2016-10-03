package com.notnotme.jsparser.ui.view;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableRow;

import java.util.ResourceBundle;

public class EditorTreeTableRow<T> extends TreeTableRow<T> {

	private ResourceBundle mResourceBundle;

	public EditorTreeTableRow(ResourceBundle resourceBundle) {
		mResourceBundle = resourceBundle;
	}

	@Override
	protected void updateItem(T item, boolean empty) {
		super.updateItem(item, empty);
		TreeItem treeItem = getTreeItem();
		if (treeItem != null && treeItem.getChildren().isEmpty()) {
			setContextMenu(null);
		} else {
			setContextMenu(createContextMenu());
		}
	}

	private ContextMenu createContextMenu() {
		ContextMenu contextMenu = new ContextMenu();

		MenuItem expandAllitems = new MenuItem(mResourceBundle.getString("expand_all"));
		MenuItem collapseAllitems = new MenuItem(mResourceBundle.getString("collapse_all"));

		expandAllitems.setOnAction(event -> {
			TreeItem item = getTreeItem();
			item.setExpanded(true);
			expandAllChildren(item);
			event.consume();
		});
		collapseAllitems.setOnAction(event -> {
			TreeItem item = getTreeItem();
			item.setExpanded(false);
			collapseAllChildren(item);
			event.consume();
		});

		contextMenu.getItems().addAll(expandAllitems, collapseAllitems);

		return contextMenu;
	}

	private static void expandAllChildren(TreeItem item) {
		item.getChildren().forEach(o -> {
			TreeItem other = (TreeItem) o;
			if (!other.getChildren().isEmpty()) {
				other.setExpanded(true);
				expandAllChildren(other);
			} else {
				other.setExpanded(true);
			}
		});
	}

	private static void collapseAllChildren(TreeItem item) {
		item.getChildren().forEach(o -> {
			TreeItem other = (TreeItem) o;
			if (!other.getChildren().isEmpty()) {
				other.setExpanded(false);
				expandAllChildren(other);
			} else {
				other.setExpanded(false);
			}
		});
	}

}
