package net.rubygrapefruit.docs.model;

public class BuildableTextInline {
    private StringBuilder text = new StringBuilder();

    public String getText() {
        return text.toString();
    }

    public void append(char ch) {
        this.text.append(ch);
    }

    public void append(CharSequence charSequence) {
        this.text.append(charSequence);
    }
}
