package net.rubygrapefruit.docs.renderer;

import net.rubygrapefruit.docs.model.Document;
import net.rubygrapefruit.docs.theme.Theme;

import java.io.File;

public abstract class Renderer {
    public abstract void render(Document document, Theme theme, File outputFile) throws RenderException;
}
