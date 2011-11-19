package net.rubygrapefruit.docs.model;

import java.util.LinkedList;

public class DocumentBuilder {
    private final LinkedList<BuildableSection> sections = new LinkedList<BuildableSection>();

    public DocumentBuilder(BuildableDocument document) {
        sections.add(document);
    }

    public BuildableParagraph appendParagraph() {
        return sections.getLast().addParagraph();
    }

    public BuildableSection appendSection(int depth) {
        if (depth < sections.size()) {
            sections.subList(depth, sections.size()).clear();
        }
        while (depth > sections.size()) {
            BuildableSection parent = sections.getLast().addSection();
            sections.add(parent);
        }
        BuildableSection section = sections.getLast().addSection();
        sections.add(section);
        return section;
    }
}
