package net.rubygrapefruit.docs.theme;

public class MinimalTheme implements Theme {
    public RenderableDocumentBuilder getDocumentBuilder() {
        return new SingleChunkBuilder();
    }

    public <T extends Aspect> T getAspect(Class<T> type) {
        return null;
    }
}
