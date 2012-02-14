package net.rubygrapefruit.docs.renderer;

import net.rubygrapefruit.docs.model.Component;

public class BuildableTitleBlock implements TitleBlock {
    private Component component;

    public BuildableTitleBlock(Component component) {
        this.component = component;
    }

    public Component getComponent() {
        return component;
    }

    public void setComponent(Component component) {
        this.component = component;
    }
}
