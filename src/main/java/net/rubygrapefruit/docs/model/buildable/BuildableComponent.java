package net.rubygrapefruit.docs.model.buildable;

import net.rubygrapefruit.docs.model.Action;
import net.rubygrapefruit.docs.model.Component;
import net.rubygrapefruit.docs.model.Referenceable;

import java.util.ArrayList;
import java.util.List;

public abstract class BuildableComponent extends BuildableTitledBlockContainer implements Component, BuildableBlock {
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

    public String getReferenceText() {
        return getTitle().getText();
    }

    @Override
    public String toString() {
        return String.format("[%s id:%s title:%s]", getTypeName().toLowerCase(), id, getTitle().getText());
    }

    public List<? extends BuildableComponent> getComponents() {
        return components;
    }

    public boolean contains(Referenceable element) {
        if (element == this || components.contains(element)) {
            return true;
        }
        for (BuildableComponent component : components) {
            if (component.contains(element)) {
                return true;
            }
        }
        return false;
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
