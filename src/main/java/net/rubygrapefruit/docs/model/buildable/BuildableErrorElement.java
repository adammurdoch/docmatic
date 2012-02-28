package net.rubygrapefruit.docs.model.buildable;

public class BuildableErrorElement implements net.rubygrapefruit.docs.model.Error, BuildableBlock, BuildableInline {
    private final String message;

    public BuildableErrorElement(String message) {
        this.message = message;
    }

    public void finish() {
    }

    public String getText() {
        return message;
    }

    public String getMessage() {
        return message;
    }
}
