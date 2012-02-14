package net.rubygrapefruit.docs.renderer;

import java.util.ArrayList;
import java.util.List;

/**
 * A model that represents a document that has been transformed ready for rendering.
 */
public class RenderableDocument {
    private final ArrayList<BuildableChunk> chunks = new ArrayList<BuildableChunk>();

    public List<BuildableChunk> getContents() {
        return chunks;
    }

    public BuildableChunk addChunk() {
        BuildableChunk chunk = new BuildableChunk();
        chunks.add(chunk);
        return chunk;
    }
}
