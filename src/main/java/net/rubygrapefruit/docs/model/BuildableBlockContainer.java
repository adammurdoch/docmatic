package net.rubygrapefruit.docs.model;

import java.util.ArrayList;
import java.util.List;

public class BuildableBlockContainer implements BlockContainer {
    private final List<BuildableBlock> contents = new ArrayList<BuildableBlock>();

    public void finish() {
        for (BuildableBlock block : contents) {
            block.finish();
        }
    }

    public List<? extends BuildableBlock> getContents() {
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

    protected <T extends BuildableBlock> T add(T block) {
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

    public BuildableErrorElement addError(String message) {
        return add(new BuildableErrorElement(message));
    }
}
