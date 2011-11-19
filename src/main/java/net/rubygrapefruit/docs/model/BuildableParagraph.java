package net.rubygrapefruit.docs.model;

public class BuildableParagraph implements Paragraph {
    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
