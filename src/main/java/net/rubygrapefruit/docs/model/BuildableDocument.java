package net.rubygrapefruit.docs.model;

import java.util.ArrayList;
import java.util.List;

public class BuildableDocument implements Document {
    private final List<BuildableParagraph> paragraphs = new ArrayList<BuildableParagraph>();

    public List<? extends BuildableParagraph> getParagraphs() {
        return paragraphs;
    }
    
    public BuildableParagraph addParagraph() {
        BuildableParagraph paragraph = new BuildableParagraph();
        paragraphs.add(paragraph);
        return paragraph;
    }
}
