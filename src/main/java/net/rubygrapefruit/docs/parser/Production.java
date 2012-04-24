package net.rubygrapefruit.docs.parser;

public interface Production<T> {
    void match(TokenStream<? super T> stream);
}
