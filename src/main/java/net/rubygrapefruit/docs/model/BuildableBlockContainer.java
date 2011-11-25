package net.rubygrapefruit.docs.model;

import java.util.ArrayList;
import java.util.List;

public class BuildableBlockContainer implements BlockContainer {
    private final List<Block> contents = new ArrayList<Block>();

    public List<? extends Block> getContents() {
        return contents;
    }

    public <T extends Block> List<T> getContents(Class<T> type) {
        List<T> matches = new ArrayList<T>();
        for (Block content : contents) {
            if (type.isInstance(content)) {
                matches.add(type.cast(content));
            }
        }
        return matches;
    }

    protected <T extends Block> T add(T block) {
        contents.add(block);
        return block;
    }

    public BuildableParagraph addParagraph() {
        return add(new BuildableParagraph());
    }

    public BuildableItemisedList addItemisedList() {
        return add(new BuildableItemisedList());
    }

    public BuildableOrderedList addOrderedList() {
        return add(new BuildableOrderedList());
    }

    public DefaultUnknown addUnknown(String name, final String fileName, final int lineNumber, final int columnNumber) {
        Location location = new Location() {
            public String getFile() {
                return fileName;
            }

            public int getLine() {
                return lineNumber;
            }

            public int getColumn() {
                return columnNumber;
            }
        };
        DefaultUnknown block = new DefaultUnknown(name, location);
        contents.add(block);
        return block;
    }
}
