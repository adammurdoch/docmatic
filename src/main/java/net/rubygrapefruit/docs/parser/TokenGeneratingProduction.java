package net.rubygrapefruit.docs.parser;

public interface TokenGeneratingProduction<S, T> {
    boolean match(S stream, TokenConsumer<T> consumer);
}
