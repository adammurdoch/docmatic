package net.rubygrapefruit.docs.model;

public class BuildableParagraph implements Paragraph {
    private StringBuilder text = new StringBuilder();

    public String getText() {
        return text.toString();
    }

    public void append(CharSequence text) {
        this.text.append(text);
    }
}
