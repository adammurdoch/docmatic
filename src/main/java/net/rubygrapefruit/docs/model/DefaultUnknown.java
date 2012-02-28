package net.rubygrapefruit.docs.model;

public class DefaultUnknown implements Unknown, BuildableBlock, BuildableInline {
    private final String message;

    public DefaultUnknown(String message) {
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
