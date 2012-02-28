package net.rubygrapefruit.docs.model.buildable;

import net.rubygrapefruit.docs.model.Section;

public class BuildableSection extends BuildableComponent implements Section {
    public String getTypeName() {
        return "Section";
    }
}
