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
        pages.put(firstChunk, new Page(firstChunk, outputFile, null, chunks.size()>1 ? outputDir.getName() + "/page1.html" : null, null));

        for (int i = 1; i < chunks.size(); i++) {
            BuildableChunk chunk = chunks.get(i);
            String home = "../" + outputFile.getName();
            String previous;
            if (i == 1) {
                previous = home;
            } else {
                previous = "page" + (i - 1) + ".html";
            }
            String next = null;
            if (i < chunks.size() - 1) {
                next = "page" + (i + 1) + ".html";
            }

            File pageFile = new File(outputDir, "page" + i + ".html");
            pages.put(chunk, new Page(chunk, pageFile, home, next, previous));
        }
    }

    public Page getPageFor(Chunk chunk) {
        return pages.get(chunk);
    }
}
