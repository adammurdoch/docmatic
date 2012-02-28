package net.rubygrapefruit.docs.renderer;

import net.rubygrapefruit.docs.model.Referenceable;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PageRegistry {
    private final Map<Chunk, Page> pages = new LinkedHashMap<Chunk, Page>();
    private final Map<Referenceable, Page> cache = new HashMap<Referenceable, Page>();
    private FrontPage frontPage;

    public PageRegistry(RenderableDocument document, File outputFile) {
        List<BuildableChunk> chunks = document.getContents();
        if (chunks.isEmpty()) {
            return;
        }

        File outputDir = new File(outputFile.getParentFile(), outputFile.getName() + ".content");

        BuildableChunk firstChunk = chunks.get(0);
        frontPage = new FrontPage(firstChunk, outputFile, outputDir);
        pages.put(firstChunk, frontPage);
        PageImpl previous = frontPage;

        for (int i = 1; i < chunks.size(); i++) {
            BuildableChunk chunk = chunks.get(i);
            File pageFile = new File(outputDir, String.format("%s.html", chunk.getId()));
            OtherPage page = new OtherPage(chunk, pageFile);
            page.previous = previous;
            previous.next = page;
            pages.put(chunk, page);
            previous = page;
        }
    }

    public Page getPageFor(Chunk chunk) {
        return pages.get(chunk);
    }

    public Page getPageFor(Referenceable element) {
        Page page = cache.get(element);
        if (page == null) {
            for (Page candidate : pages.values()) {
                if (candidate.getChunk().contains(element)) {
                    page = candidate;
                    break;
                }
            }
            if (page == null) {
                throw new IllegalArgumentException("Element " + element + " not found.");
            }
            cache.put(element, page);
        }
        return page;
    }

    private abstract class PageImpl extends Page {
        PageImpl next;
        PageImpl previous;

        private PageImpl(Chunk chunk, File file) {
            super(chunk, file);
        }

        @Override
        public String getNextUrl() {
            return next == null ? null : getUrlTo(next);
        }

        @Override
        public String getPreviousUrl() {
            return previous == null ? null : getUrlTo(previous);
        }

        @Override
        public Page getPageFor(Referenceable element) {
            return PageRegistry.this.getPageFor(element);
        }
    }

    private class OtherPage extends PageImpl {
        public OtherPage(BuildableChunk chunk, File pageFile) {
            super(chunk, pageFile);
        }

        @Override
        public String getHomeUrl() {
            return "../" + frontPage.getFile().getName();
        }

        @Override
        public String getUrlTo(Page other) {
            if (other == this) {
                return null;
            }
            if (other == frontPage) {
                return getHomeUrl();
            }
            return other.getFile().getName();
        }
    }

    private class FrontPage extends PageImpl {
        private final File contentDir;

        private FrontPage(Chunk chunk, File file, File contentDir) {
            super(chunk, file);
            this.contentDir = contentDir;
        }

        @Override
        public String getHomeUrl() {
            return null;
        }

        @Override
        public String getUrlTo(Page other) {
            if (other == this) {
                return null;
            }
            return contentDir.getName() + "/" + other.getFile().getName();
        }
    }
}
