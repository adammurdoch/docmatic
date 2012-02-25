package net.rubygrapefruit.docs.renderer;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PageRegistry {
    private final Map<Chunk, Page> pages = new LinkedHashMap<Chunk, Page>();

    public PageRegistry(RenderableDocument document, File outputFile) {
        List<BuildableChunk> chunks = document.getContents();
        if (chunks.isEmpty()) {
            return;
        }

        File outputDir = new File(outputFile.getParentFile(), outputFile.getName() + ".content");

        BuildableChunk firstChunk = chunks.get(0);
        String nextUrl = chunks.size() > 1 ? String.format("%s/%s.html", outputDir.getName(), chunks.get(1).getId()) : null;
        pages.put(firstChunk, new Page(firstChunk, outputFile, null, nextUrl, null));

        for (int i = 1; i < chunks.size(); i++) {
            BuildableChunk chunk = chunks.get(i);
            String home = "../" + outputFile.getName();
            String previous;
            if (i == 1) {
                previous = home;
            } else {
                previous = String.format("%s.html", chunks.get(i-1).getId());
            }
            String next = null;
            if (i < chunks.size() - 1) {
                next = String.format("%s.html", chunks.get(i+1).getId());
            }

            File pageFile = new File(outputDir, String.format("%s.html", chunk.getId()));
            pages.put(chunk, new Page(chunk, pageFile, home, next, previous));
        }
    }

    public Page getPageFor(Chunk chunk) {
        return pages.get(chunk);
    }
}
