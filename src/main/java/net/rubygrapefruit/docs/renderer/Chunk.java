package net.rubygrapefruit.docs.renderer;

import net.rubygrapefruit.docs.model.Block;
import net.rubygrapefruit.docs.model.Referenceable;

import java.util.List;

/**
 * A 'chunk' of a document.
 */
public interface Chunk {
    String getId();

    List<? extends Block> getContents();

    boolean contains(Referenceable element);
}
