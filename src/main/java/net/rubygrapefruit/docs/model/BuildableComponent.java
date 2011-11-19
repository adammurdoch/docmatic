package net.rubygrapefruit.docs.model;

public class BuildableComponent extends BuildableContainer implements Component {
    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(CharSequence title) {
        this.title = title.toString();
    }

    public BuildableSection addSection() {
        return add(new BuildableSection());
    }
}
