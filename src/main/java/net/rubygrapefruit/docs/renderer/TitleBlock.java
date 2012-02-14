package net.rubygrapefruit.docs.renderer;

import net.rubygrapefruit.docs.model.Block;
import net.rubygrapefruit.docs.model.Component;

/**
 * A title page for a component.
 */
public interface TitleBlock extends Block {
    /**
     * Returns the target component.
     */
    Component getComponent();
}
