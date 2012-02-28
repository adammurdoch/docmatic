package net.rubygrapefruit.docs.model;

/**
 * A {@link LinkTarget} that references an element in the current document.
 */
public interface InternalTarget extends LinkTarget {
    Referenceable getElement();
}
