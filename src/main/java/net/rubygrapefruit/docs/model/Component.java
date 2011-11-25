package net.rubygrapefruit.docs.model;

/**
 * A component is a group of block elements plus some meta-information, such as a title.
 */
public interface Component extends BlockContainer {
    Title getTitle();
}
