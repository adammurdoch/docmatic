package net.rubygrapefruit.docs.model.buildable;

import net.rubygrapefruit.docs.model.Part;

public class BuildablePart extends BuildableComponent implements Part {
    public String getTypeName() {
        return "Part";
    }
}
