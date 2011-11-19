package net.rubygrapefruit.docs.model;

import java.util.ArrayList;
import java.util.List;

public class BuildableContainer implements Container {
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
        BuildableParagraph paragraph = new BuildableParagraph();
        contents.add(paragraph);
        return paragraph;
    }

    public DefaultUnknownBlock addUnknown(String name, final String fileName, final int lineNumber, final int columnNumber) {
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
        DefaultUnknownBlock block = new DefaultUnknownBlock(name, location);
        contents.add(block);
        return block;
    }
}
