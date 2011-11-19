package net.rubygrapefruit.docs.parser;

public class ParseException extends RuntimeException {
    public ParseException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
