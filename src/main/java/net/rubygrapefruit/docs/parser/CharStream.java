package net.rubygrapefruit.docs.parser;

public interface CharStream extends RewindableStream {
    boolean consumeRange(char from, char to);

    boolean consume(char... candidates);

    boolean consumeAnyExcept(char... candidates);

    /**
     * Consumes a single instance of the given production.
     *
     * @return true if the production consumed any characters, false if not
     */
    boolean consume(CharProduction production);

    /**
     * Consumes at least one instance of the given production.
     *
     * @return true if the production consumed any characters, false if not
     */
    boolean consumeAtLeastOne(CharProduction production);
}
