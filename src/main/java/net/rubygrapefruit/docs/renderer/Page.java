package net.rubygrapefruit.docs.renderer;

import net.rubygrapefruit.docs.model.Nullable;
import net.rubygrapefruit.docs.model.Referenceable;

import java.io.File;

public abstract class Page {
    private final Chunk chunk;
    private final File file;

    Page(Chunk chunk, File file) {
        this.chunk = chunk;
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public Chunk getChunk() {
        return chunk;
    }

    @Nullable
    public abstract String getHomeUrl();

    @Nullable
    public abstract String getNextUrl();

    @Nullable
    public abstract String getPreviousUrl();

    /**
     * Returns null when other == this
     */
    @Nullable
    public abstract String getUrlTo(Page other);

    public abstract Page getPageFor(Referenceable element);
}
