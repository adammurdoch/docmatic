package net.rubygrapefruit.docs.model;

import java.util.ArrayList;
import java.util.List;

public class BuildableSection implements Section {
    private final List<Block> contents = new ArrayList<Block>();
    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(CharSequence title) {
        this.title = title.toString();
    }

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

    public BuildableParagraph addParagraph() {
        BuildableParagraph paragraph = new BuildableParagraph();
        contents.add(paragraph);
        return paragraph;
    }
    
    public BuildableSection addSection() {
        BuildableSection section = new BuildableSection();
        contents.add(section);
        return section;
    }
}
