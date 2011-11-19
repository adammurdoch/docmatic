package net.rubygrapefruit.docs.model;

import java.util.List;

/**
 * A section is a structural block element, containing sections and other blocks.
 */
public interface Section extends Block {
    String getTitle();

    /**
     * Returns the blocks contained in this section.
     */
    List<? extends Block> getContents();

    /**
     * Returns the blocks contained in this section.
     */
    <T extends Block> List<T> getContents(Class<T> type);
}
