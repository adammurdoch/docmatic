package net.rubygrapefruit.docs.renderer;

import java.io.File;

public class Page {
    private final Chunk chunk;
    private final File file;
    private final String homeUrl;
    private final String nextUrl;
    private final String previousUrl;

    Page(Chunk chunk, File file, String homeUrl, String nextUrl, String previousUrl) {
        this.chunk = chunk;
        this.file = file;
        this.homeUrl = homeUrl;
        this.nextUrl = nextUrl;
        this.previousUrl = previousUrl;
    }

    public File getFile() {
        return file;
    }

    public Chunk getChunk() {
        return chunk;
    }

    public String getHomeUrl() {
        return homeUrl;
    }

    public String getNextUrl() {
        return nextUrl;
    }

    public String getPreviousUrl() {
        return previousUrl;
    }
}