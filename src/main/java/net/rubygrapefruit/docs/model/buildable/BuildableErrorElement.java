package net.rubygrapefruit.docs.model.buildable;

import net.rubygrapefruit.docs.model.Error;

public class BuildableErrorElement implements Error, BuildableBlock, BuildableInline {
    private final String message;

    public BuildableErrorElement(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return String.format("[error %s]", message);
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
