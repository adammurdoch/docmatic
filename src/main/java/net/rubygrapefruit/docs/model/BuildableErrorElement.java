package net.rubygrapefruit.docs.model;

public class BuildableErrorElement implements Error, BuildableBlock, BuildableInline {
    private final String message;

    public BuildableErrorElement(String message) {
        this.message = message;
    }

    public void finish() {
    }

    public String getText() {
        return "";
    }

    public String getMessage() {
        return message;
    }
}
