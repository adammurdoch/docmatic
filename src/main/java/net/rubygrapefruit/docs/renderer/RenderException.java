package net.rubygrapefruit.docs.renderer;

public class RenderException extends RuntimeException {
    public RenderException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
