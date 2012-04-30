package net.rubygrapefruit.docs.parser;

public interface CharProduction extends Production<CharStream> {
    void match(CharStream charStream);
}
