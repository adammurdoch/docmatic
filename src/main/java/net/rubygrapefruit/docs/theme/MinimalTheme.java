package net.rubygrapefruit.docs.theme;

import net.rubygrapefruit.docs.renderer.TextTheme;

public class MinimalTheme implements Theme {
    public RenderableDocumentBuilder getDocumentBuilder() {
        return new SingleChunkBuilder();
    }

    public TextTheme asTextTheme() {
        return null;
    }
}
