package net.rubygrapefruit.docs.model;

import java.util.*;
import java.util.List;

public class BuildableInlineContainer implements InlineContainer {
    private final BuildableText text = new BuildableText();

    public String getText() {
        return text.getText();
    }

    public void append(CharSequence text) {
        this.text.append(text);
    }

    public List<? extends Inline> getContents() {
        return Arrays.asList(text);
    }
}
