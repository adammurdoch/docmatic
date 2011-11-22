package net.rubygrapefruit.docs.renderer;

import net.rubygrapefruit.docs.model.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public abstract class Renderer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Renderer.class);

    public void render(Document document, Theme theme, File output) throws RenderException {
        LOGGER.info("Generating {}.", output);
        try {
            output.getParentFile().mkdirs();
            BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(output));
            try {
                doRender(document, theme, stream);
            } finally {
                stream.close();
            }
        } catch (Exception e) {
            throw new RenderException(String.format("Could not render document to '%s'.", output), e);
        }
    }

    public void render(Document document, Theme theme, OutputStream stream) throws RenderException {
        try {
            doRender(document, theme, stream);
        } catch (Exception e) {
            throw new RenderException("Could not render document.", e);
        }
    }

    protected abstract void doRender(Document document, Theme theme, OutputStream stream) throws Exception;
}
