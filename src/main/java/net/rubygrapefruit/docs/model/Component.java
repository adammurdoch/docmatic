package net.rubygrapefruit.docs.model;

import java.util.List;

/**
 * A component is a structural element that contains other structural elements and some meta-information, such as a title.
 */
public interface Component extends BlockContainer, Block {
    Title getTitle();
    
    List<? extends Component> getComponents();
}
