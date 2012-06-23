package net.rubygrapefruit.docs.model.buildable;

import net.rubygrapefruit.docs.model.ProgramListing;

public class BuildableProgramListing implements BuildableBlock, ProgramListing {
    private final StringBuilder text = new StringBuilder();

    public void finish() {
    }

    public void append(String text) {
        this.text.append(text);
    }

    public String getText() {
        return text.toString();
    }
}
