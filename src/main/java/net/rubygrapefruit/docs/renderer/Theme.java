package net.rubygrapefruit.docs.renderer;

public interface Theme {
    /**
     * Returns the text components of this theme, if any.
     *
     * @return The text components, or null.
     */
    public TextTheme asTextTheme();
}
