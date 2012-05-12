package net.rubygrapefruit.docs.parser;

public interface RewindableStream {
    /**
     * Moves the cursor back to the mark. Does not affect the mark.
     */
    void rewind();
}
