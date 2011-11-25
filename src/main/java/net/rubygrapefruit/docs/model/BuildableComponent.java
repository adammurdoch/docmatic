package net.rubygrapefruit.docs.model;

public class BuildableComponent extends BuildableBlockContainer implements Component {
    private final BuildableTitle title = new BuildableTitle();
    private BuildableComponent current;

    public BuildableTitle getTitle() {
        return title;
    }

    public BuildableSection addSection() {
        BuildableSection section = new BuildableSection();
        add(section);
        current = section;
        return section;
    }

    public BuildableSection addSection(int depth) {
        assert depth > 0;
        if (depth == 1) {
            return addSection();
        }
        if (current == null) {
            return addSection().addSection(depth - 1);
        }
        return current.addSection(depth - 1);
    }

    public BuildableComponent getCurrent() {
        return current == null ? this : current.getCurrent();
    }
}
