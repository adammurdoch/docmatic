package net.rubygrapefruit.docs.theme;

import net.rubygrapefruit.docs.renderer.TextTheme;

public interface Theme {
    /**
     * Returns the text components of this theme, if any.
     *
     * @return The text components, or null.
     */
    public TextTheme asTextTheme();
}
