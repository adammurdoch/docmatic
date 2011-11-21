package net.rubygrapefruit.docs.model;

import java.util.LinkedList;

public class DocumentBuilder {
    private final LinkedList<BuildableComponent> components = new LinkedList<BuildableComponent>();

    public DocumentBuilder(BuildableComponent owner) {
        components.add(owner);
    }

    public DefaultUnknownBlock appendUnknown(String name, final String fileName, final int lineNumber, final int columnNumber) {
        return getCurrentContainer().addUnknown(name, fileName, lineNumber, columnNumber);
    }

    public BuildableComponent getCurrentComponent() {
        return components.getLast();
    }

    public BuildableContainer getCurrentContainer() {
        return getCurrentComponent();
    }

    public BuildableSection appendSection() {
        BuildableSection section = components.getLast().addSection();
        components.add(section);
        return section;
    }

    public BuildableSection appendSection(int depth) {
        if (depth < components.size()) {
            components.subList(depth, components.size()).clear();
        }
        while (depth > components.size()) {
            BuildableSection parent = components.getLast().addSection();
            components.add(parent);
        }
        return appendSection();
    }

    public BuildableComponent popSection() {
        assert components.size() > 1;
        return components.removeLast();
    }
}
