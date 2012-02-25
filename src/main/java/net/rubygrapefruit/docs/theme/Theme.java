package net.rubygrapefruit.docs.theme;

import net.rubygrapefruit.docs.model.Nullable;

public interface Theme {
    /**
     * Returns the builder to use to generate the renderable document for this theme. Allows the theme to modify
     * the structure.
     */
    RenderableDocumentBuilder getDocumentBuilder();

    /**
     * Returns the given aspect of this theme, if any.
     *
     * @return The aspect, or null.
     */
    @Nullable
    public <T extends Aspect> T getAspect(Class<T> type);
}
