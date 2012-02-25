package net.rubygrapefruit.docs.model;

import java.util.ArrayList;
import java.util.List;

public abstract class BuildableComponent extends BuildableBlockContainer implements Component {
    private final BuildableTitle title = new BuildableTitle();
    private final List<BuildableComponent> components = new ArrayList<BuildableComponent>();
    private BuildableComponent current;
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id.trim();
        if (this.id.isEmpty()) {
            this.id = null;
        }
    }

    public BuildableTitle getTitle() {
        return title;
    }

    public List<? extends Component> getComponents() {
        return components;
    }

    /**
     * Visits this component and all its descendents
     */
    public void visitAllComponents(Action<? super BuildableComponent> action) {
        action.execute(this);
        for (BuildableComponent component : components) {
            component.visitAllComponents(action);
        }
    }

    private <T extends BuildableComponent> T addComponent(T component) {
        add(component);
        components.add(component);
        current = component;
        return component;
    }

    public BuildableSection addSection() {
        return addComponent(new BuildableSection());
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

    public BuildablePart addPart() {
        return addComponent(new BuildablePart());
    }

    public BuildableChapter addChapter() {
        return addComponent(new BuildableChapter());
    }

    public BuildableAppendix addAppendix() {
        return addComponent(new BuildableAppendix());
    }
}
