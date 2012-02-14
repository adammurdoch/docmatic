package net.rubygrapefruit.docs.theme;

import net.rubygrapefruit.docs.renderer.TextTheme;

public interface Theme {
    /**
     * Returns the builder to use to generate the renderable document for this theme. Allows the theme to modify
     * the structure.
     */
    RenderableDocumentBuilder getDocumentBuilder();

    /**
     * Returns the text components of this theme, if any.
     *
     * @return The text components, or null.
     */
    public TextTheme asTextTheme();
}
