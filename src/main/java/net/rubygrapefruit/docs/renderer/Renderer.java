package net.rubygrapefruit.docs.renderer;

import net.rubygrapefruit.docs.model.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public abstract class Renderer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Renderer.class);

    public void render(Document document, File output) {
        LOGGER.info("Generating {}.", output);
        try {
            output.getParentFile().mkdirs();
            BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(output));
            try {
                render(document, stream);
            } finally {
                stream.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(String.format("Could not render document to '%s'.", output), e);
        }
    }

    public abstract void render(Document document, OutputStream stream);
}
