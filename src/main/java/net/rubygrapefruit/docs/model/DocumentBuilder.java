package net.rubygrapefruit.docs.model;

import java.util.LinkedList;

public class DocumentBuilder {
    private final LinkedList<BuildableComponent> sections = new LinkedList<BuildableComponent>();

    public DocumentBuilder(BuildableComponent owner) {
        sections.add(owner);
    }

    public BuildableParagraph appendParagraph() {
        return getCurrentSection().addParagraph();
    }

    public BuildableComponent getCurrentSection() {
        return sections.getLast();
    }

    public BuildableSection appendSection() {
        BuildableSection section = sections.getLast().addSection();
        sections.add(section);
        return section;
    }

    public BuildableSection appendSection(int depth) {
        if (depth < sections.size()) {
            sections.subList(depth, sections.size()).clear();
        }
        while (depth > sections.size()) {
            BuildableSection parent = sections.getLast().addSection();
            sections.add(parent);
        }
        return appendSection();
    }

    public BuildableComponent popSection() {
        assert sections.size() > 1;
        return sections.removeLast();
    }

    public DefaultUnknownBlock appendUnknown(String name, final String fileName, final int lineNumber, final int columnNumber) {
        return getCurrentSection().addUnknown(name, fileName, lineNumber, columnNumber);
    }
}
