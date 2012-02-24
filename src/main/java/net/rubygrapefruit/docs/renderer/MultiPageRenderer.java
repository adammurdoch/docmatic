package net.rubygrapefruit.docs.renderer;

import net.rubygrapefruit.docs.model.Document;
import net.rubygrapefruit.docs.theme.Theme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public abstract class MultiPageRenderer extends Renderer {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultiPageRenderer.class);

    public void render(Document document, Theme theme, File outputFile) throws RenderException {
        RenderableDocument renderableDocument = new RenderableDocument();
        theme.getDocumentBuilder().buildDocument(document, renderableDocument);

        PageRegistry registry = new PageRegistry(renderableDocument, outputFile);
        for (Chunk chunk : renderableDocument.getContents()) {
            Page page = registry.getPageFor(chunk);
            renderPage(theme, page);
        }
    }

    private void renderPage(Theme theme, Page page) {
        File outputFile = page.getFile();
        LOGGER.info("Generating {}.", outputFile);
        try {
            outputFile.getParentFile().mkdirs();
            BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(outputFile));
            try {
                doRender(page, theme, stream);
            } finally {
                stream.close();
            }
        } catch (Exception e) {
            throw new RenderException(String.format("Could not render document to '%s'.", outputFile), e);
        }
    }

    protected abstract void doRender(Page page, Theme theme, OutputStream stream) throws Exception;
}
