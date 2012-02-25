package net.rubygrapefruit.docs.theme;

import net.rubygrapefruit.docs.model.*;
import net.rubygrapefruit.docs.renderer.BuildableChunk;
import net.rubygrapefruit.docs.renderer.RenderableDocument;

public class MultipleChunkBuilder implements RenderableDocumentBuilder {
    public void buildDocument(Document source, RenderableDocument target) {
        addChunks(source, target);
        if (target.getContents().isEmpty()) {
            target.addChunk().setId(source.getId());
        }
    }

    private void addChunks(Component component, RenderableDocument target) {
        if (!component.getTitle().isEmpty()) {
            BuildableChunk titlePage = target.addChunk();
            titlePage.setId(component.getId());
            titlePage.addTitlePage(component);
        }

        BuildableChunk current = null;
        for (Block block : component.getContents()) {
            if (block instanceof Part) {
                Part part = (Part) block;
                addChunks(part, target);
                current = null;
            } else if (block instanceof Component) {
                Component childComponent = (Component) block;
                current = target.addChunk();
                current.setId(childComponent.getId());
                current.add(block);
                current = null;
            } else {
                if (current == null) {
                    current = target.addChunk();
                    current.setId("page" + target.getContents().size());
                }
                current.add(block);
            }
        }
    }
}
