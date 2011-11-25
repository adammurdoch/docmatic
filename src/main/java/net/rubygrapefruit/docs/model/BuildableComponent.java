package net.rubygrapefruit.docs.model;

public class BuildableComponent extends BuildableBlockContainer implements Component {
    private final BuildableTitle title = new BuildableTitle();

    public BuildableTitle getTitle() {
        return title;
    }

    public BuildableSection addSection() {
        return add(new BuildableSection());
    }
}
