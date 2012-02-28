package net.rubygrapefruit.docs.model.buildable;

import net.rubygrapefruit.docs.model.ListItem;

import java.util.ArrayList;
import java.util.List;

public class BuildableList implements BuildableBlock {
    private final List<BuildableListItem> items = new ArrayList<BuildableListItem>();

    public void finish() {
        for (BuildableListItem item : items) {
            item.finish();
        }
    }

    public List<? extends ListItem> getItems() {
        return items;
    }

    public BuildableListItem addItem() {
        BuildableListItem item = new BuildableListItem();
        items.add(item);
        return item;
    }
}
