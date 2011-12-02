package net.rubygrapefruit.docs.model;

import java.util.List;

/**
 * A container of {@link Inline} elements.
 */
public interface InlineContainer {
    /**
     * Returns true if this container has no contents.
     */
    boolean isEmpty();

    List<? extends Inline> getContents();

    /**
     * Returns the text content of this container with all markup removed.
     *
     * @return the text content
     */
    String getText();
}
