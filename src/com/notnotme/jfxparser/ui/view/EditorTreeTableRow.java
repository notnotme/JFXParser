package com.notnotme.jfxparser.ui.view;

import javafx.scene.control.*;
import javafx.util.Pair;

import java.util.ResourceBundle;

public class EditorTreeTableRow<T> extends TreeTableRow<Pair<String, T>> {

    private final ResourceBundle mResourceBundle;
    private final ContextMenu mContextMenu;

    public EditorTreeTableRow(ResourceBundle resourceBundle) {
        mResourceBundle = resourceBundle;
        mContextMenu = createContextMenu();
    }

    @Override
    protected void updateItem(Pair<String, T> item, boolean empty) {
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
            TreeItem<Pair<String, T>> item = getTreeItem();
            // fixme: If a crash occur here uncomment below
            // if (item == null) return;

            item.setExpanded(true);
            expandAllChildren(item, true);
            event.consume();
        });
        collapseAllItems.setOnAction(event -> {
            TreeItem<Pair<String, T>> item = getTreeItem();
            item.setExpanded(false);
            expandAllChildren(item, false);
            event.consume();
        });
        expandAllChildrenItems.setOnAction(event -> {
            TreeItem<Pair<String, T>> item = getTreeItem();
            expandAllChildren(item, true);
            event.consume();
        });
        collapseAllChildrenItems.setOnAction(event -> {
            TreeItem<Pair<String, T>> item = getTreeItem();
            expandAllChildren(item, false);
            event.consume();
        });

        contextMenu.getItems().addAll(expandAllItems, collapseAllItems, separatorItem,
                expandAllChildrenItems, collapseAllChildrenItems);

        return contextMenu;
    }

    private static <T> void expandAllChildren(TreeItem<Pair<String, T>> item, boolean expand) {
        item.getChildren().forEach(o -> {
            if (!o.getChildren().isEmpty()) {
                o.setExpanded(expand);
                expandAllChildren(o, expand);
            } else {
                o.setExpanded(!expand);
            }
        });
    }

}
