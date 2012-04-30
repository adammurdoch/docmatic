package net.rubygrapefruit.docs.parser;

public interface TokenConsumer<T> {
    void consume(T token);
}
