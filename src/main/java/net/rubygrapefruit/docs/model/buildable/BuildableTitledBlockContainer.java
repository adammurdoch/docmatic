package net.rubygrapefruit.docs.model.buildable;

public class BuildableTitledBlockContainer extends BuildableBlockContainer {
    private final BuildableTitle title = new BuildableTitle();

    public BuildableTitle getTitle() {
        return title;
    }

    @Override
    public void finish() {
        title.finish();
        super.finish();
    }
}
