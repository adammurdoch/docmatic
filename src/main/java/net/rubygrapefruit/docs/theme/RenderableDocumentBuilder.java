package net.rubygrapefruit.docs.theme;

import net.rubygrapefruit.docs.model.Document;
import net.rubygrapefruit.docs.renderer.RenderableDocument;

public interface RenderableDocumentBuilder {
    void buildDocument(Document source, RenderableDocument target);
}
