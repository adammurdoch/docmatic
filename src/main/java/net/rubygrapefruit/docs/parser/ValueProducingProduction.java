package net.rubygrapefruit.docs.parser;

public interface ValueProducingProduction<S, T> {
    T match(S stream);
}
