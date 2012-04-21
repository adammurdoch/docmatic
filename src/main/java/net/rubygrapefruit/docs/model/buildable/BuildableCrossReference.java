package net.rubygrapefruit.docs.model.buildable;

import net.rubygrapefruit.docs.model.CrossReference;
import net.rubygrapefruit.docs.model.Inline;
import net.rubygrapefruit.docs.model.Referenceable;

import java.util.Arrays;
import java.util.List;

public class BuildableCrossReference extends BuildableInlineContainer implements CrossReference, BuildableInline {
    private final Referenceable target;

    public BuildableCrossReference(Referenceable target) {
        this.target = target;
    }

    public Referenceable getTarget() {
        return target;
    }

    @Override
    public List<? extends Inline> getContents() {
        List<? extends Inline> contents = super.getContents();
        if (contents.isEmpty()) {
            BuildableText text = new BuildableText();
            text.append(target.getReferenceText());
            return Arrays.asList(text);
        }
        return contents;
    }
}
