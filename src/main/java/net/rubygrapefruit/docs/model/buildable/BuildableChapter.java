package net.rubygrapefruit.docs.model.buildable;

import net.rubygrapefruit.docs.model.Chapter;

public class BuildableChapter extends BuildableComponent implements Chapter {
    public String getTypeName() {
        return "Chapter";
    }
}
