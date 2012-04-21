package net.rubygrapefruit.docs.model.buildable;

import net.rubygrapefruit.docs.model.Inline;
import net.rubygrapefruit.docs.model.Link;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

public class BuildableLink extends BuildableInlineContainer implements Link, BuildableInline {
    private final URI target;

    public BuildableLink(URI target) {
        this.target = target;
    }

    public URI getTarget() {
        return target;
    }

    @Override
    public List<? extends Inline> getContents() {
        List<? extends Inline> contents = super.getContents();
        if (contents.isEmpty()) {
            BuildableText text = new BuildableText();
            text.append(target.toString());
            return Arrays.asList(text);
        }
        return contents;
    }
}
