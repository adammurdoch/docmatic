package net.rubygrapefruit.docs.model;

/**
 * Some unexpected or broken content in the source document.
 */
public interface Error extends Block, Inline {
    String getMessage();
}
