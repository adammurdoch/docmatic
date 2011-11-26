package net.rubygrapefruit.docs.model;

public class DefaultUnknown implements Unknown {
    private final String message;

    public DefaultUnknown(String message) {
        this.message = message;
    }

    public String getText() {
        return "";
    }

    public String getMessage() {
        return message;
    }
}
