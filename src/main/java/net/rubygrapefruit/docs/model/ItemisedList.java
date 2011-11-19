package net.rubygrapefruit.docs.model;

import java.util.List;

public interface ItemisedList extends Block {
    List<? extends ListItem> getItems();
}
