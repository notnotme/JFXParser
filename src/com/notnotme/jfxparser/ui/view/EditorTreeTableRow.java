package com.notnotme.jfxparser.ui.view;

import javafx.scene.control.*;

import java.util.ResourceBundle;

public class EditorTreeTableRow<T> extends TreeTableRow<T> {

	private ResourceBundle mResourceBundle;
	private ContextMenu mContextMenu;

	public EditorTreeTableRow(ResourceBundle resourceBundle) {
		mResourceBundle = resourceBundle;
		mContextMenu = createContextMenu();
	}

	@Override
	protected void updateItem(T item, boolean empty) {
		super.updateItem(item, empty);
		TreeItem treeItem = getTreeItem();
		if (treeItem != null && treeItem.getChildren().isEmpty()) {
			setContextMenu(null);
		} else {
			setContextMenu(mContextMenu);
		}
	}

	private ContextMenu createContextMenu() {
		ContextMenu contextMenu = new ContextMenu();

		MenuItem expandAllItems = new MenuItem(mResourceBundle.getString("expand_all"));
		MenuItem collapseAllItems = new MenuItem(mResourceBundle.getString("collapse_all"));
		MenuItem separatorItem = new SeparatorMenuItem();
		MenuItem expandAllChildrenItems = new MenuItem(mResourceBundle.getString("expand_all_children"));
		MenuItem collapseAllChildrenItems = new MenuItem(mResourceBundle.getString("collapse_all_children"));

		expandAllItems.setOnAction(event -> {
			TreeItem item = getTreeItem();
			item.setExpanded(true);
			expandAllChildren(item, true);
			event.consume();
		});
		collapseAllItems.setOnAction(event -> {
			TreeItem item = getTreeItem();
			item.setExpanded(false);
			expandAllChildren(item, false);
			event.consume();
		});
		expandAllChildrenItems.setOnAction(event -> {
			TreeItem item = getTreeItem();
			expandAllChildren(item, true);
			event.consume();
		});
		collapseAllChildrenItems.setOnAction(event -> {
			TreeItem item = getTreeItem();
			expandAllChildren(item, false);
			event.consume();
		});

		contextMenu.getItems().addAll(expandAllItems, collapseAllItems, separatorItem,
				expandAllChildrenItems, collapseAllChildrenItems);

		return contextMenu;
	}

	private static void expandAllChildren(TreeItem item, boolean expand) {
		item.getChildren().forEach(o -> {
			TreeItem other = (TreeItem) o;
			if (!other.getChildren().isEmpty()) {
				other.setExpanded(expand);
				expandAllChildren(other, expand);
			} else {
				other.setExpanded(!expand);
			}
		});
	}

}
