package net.rubygrapefruit.docs.model;

import java.util.List;

/**
 * A component is a structural element that contains other structural elements and some meta-information, such as a title.
 */
public interface Component extends BlockContainer, Block, Referenceable {
    Title getTitle();

    /**
     * Returns the child components of this component.
     */
    List<? extends Component> getComponents();

    /**
     * Returns a human-readable name for the type of this component, suitable for including in generated text (eg 'Chapter', 'Section', etc).
     */
    String getTypeName();

    /**
     * Returns true if this component is or contains the given element.
     */
    boolean contains(Referenceable element);
}
