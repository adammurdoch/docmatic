package net.rubygrapefruit.docs.model;

import java.util.ArrayList;
import java.util.List;

public class BuildableItemisedList implements ItemisedList {
    private final List<ListItem> items = new ArrayList<ListItem>();
    
    public List<? extends ListItem> getItems() {
        return items;
    }
    
    public BuildableListItem addItem() {
        BuildableListItem item = new BuildableListItem();
        items.add(item);
        return item;
    }
}
