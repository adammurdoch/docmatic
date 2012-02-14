package net.rubygrapefruit.docs.theme;

import net.rubygrapefruit.docs.model.*;
import net.rubygrapefruit.docs.renderer.BuildableChunk;
import net.rubygrapefruit.docs.renderer.RenderableDocument;

public class MultipleChunkBuilder implements RenderableDocumentBuilder {
    public void buildDocument(Document source, RenderableDocument target) {
        addChunks(source, target);
    }

    private void addChunks(Component component, RenderableDocument target) {
        if (!component.getTitle().isEmpty()) {
            target.addChunk().addTitlePage(component);
        }
        BuildableChunk current = null; 
        for (Block block : component.getContents()) {
            if (block instanceof Part) {
                Part part = (Part) block;
                addChunks(part, target);
                current = null;
            } else if (block instanceof Unknown) {
                if (current == null) {
                    current = target.addChunk();
                }
                current.add(block);
            } else {
                current = target.addChunk();
                current.add(block);
            }
        }
    }
}
