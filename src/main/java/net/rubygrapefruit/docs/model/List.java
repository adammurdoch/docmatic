package net.rubygrapefruit.docs.model;

public interface List extends Block {
    java.util.List<? extends ListItem> getItems();
}
