package net.rubygrapefruit.docs.model.buildable;

import net.rubygrapefruit.docs.model.CrossReference;
import net.rubygrapefruit.docs.model.Referenceable;

public class BuildableCrossReference implements CrossReference, BuildableInline {
    private final Referenceable target;

    public BuildableCrossReference(Referenceable target) {
        this.target = target;
    }

    public Referenceable getTarget() {
        return target;
    }

    public void finish() {
    }

    public String getText() {
        return target.getReferenceText();
    }
}
