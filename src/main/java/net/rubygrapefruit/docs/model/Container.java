package net.rubygrapefruit.docs.model;

import java.util.List;

/**
 * A container for {@link Block} elements.
 */
public interface Container {
    /**
     * Returns the blocks contained in this container.
     */
    List<? extends Block> getContents();

    /**
     * Returns the blocks contained in this container.
     */
    <T extends Block> List<T> getContents(Class<T> type);
}
