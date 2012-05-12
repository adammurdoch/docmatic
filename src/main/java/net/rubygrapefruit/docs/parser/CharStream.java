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
    boolean consume(Production<? super CharStream> production);

    /**
     * Consumes a single instance of the given production.
     *
     * @return true if the production consumed any characters, false if not
     */
    <T> T consume(ValueProducingProduction<? super CharStream, T> production);

    /**
     * Consumes at least one instance of the given production.
     *
     * @return true if the production consumed any characters, false if not
     */
    boolean consumeAtLeastOne(Production<? super CharStream> production);

    /**
     * Returns the value of the most recently matched production.
     */
    String getValue();

    /**
     * Returns the start column of the most recently matched production.
     */
    int getStartColumn();

    /**
     * Returns the start line of the most recently matched production.
     */
    int getStartLine();

    /**
     * Returns the end column of the most recently matched production.
     */
    int getEndColumn();

    /**
     * Returns the end line of the most recently matched production.
     */
    int getEndLine();
}
