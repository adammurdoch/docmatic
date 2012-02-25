package net.rubygrapefruit.docs.theme;

import net.rubygrapefruit.docs.model.Block;
import net.rubygrapefruit.docs.model.Component;
import net.rubygrapefruit.docs.model.Document;
import net.rubygrapefruit.docs.model.Part;
import net.rubygrapefruit.docs.renderer.BuildableChunk;
import net.rubygrapefruit.docs.renderer.RenderableDocument;

public class SingleChunkBuilder implements RenderableDocumentBuilder {
    public void buildDocument(Document source, RenderableDocument target) {
        BuildableChunk chunk = target.addChunk();
        chunk.setId(source.getId());
        addChunks(source, chunk);
    }

    private void addChunks(Component component, BuildableChunk chunk) {
        if (!component.getTitle().isEmpty()) {
            chunk.addTitlePage(component);
        }
        for (Block block : component.getContents()) {
            if (block instanceof Part) {
                Part part = (Part) block;
                addChunks(part, chunk);
            } else {
                chunk.add(block);
            }
        }
    }
}
