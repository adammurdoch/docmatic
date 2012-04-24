package net.rubygrapefruit.docs.parser;

public interface TokenStream<T> extends RewindableStream {
    T peek();

    void consume();
}
