package net.rubygrapefruit.docs.model;

/**
 * An element that can be referred to using a cross reference.
 */
public interface Referenceable {
    /**
     * Returns the id of this element. The id is never empty, and does not begin or end with whitespace.
     */
    String getId();

    /**
     * Returns the text to use to refer to this element.
     */
    String getReferenceText();
}
