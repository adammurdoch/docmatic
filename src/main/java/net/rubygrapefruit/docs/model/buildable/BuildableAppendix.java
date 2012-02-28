package net.rubygrapefruit.docs.model.buildable;

import net.rubygrapefruit.docs.model.Appendix;

public class BuildableAppendix extends BuildableComponent implements Appendix {
    public String getTypeName() {
        return "Appendix";
    }
}
