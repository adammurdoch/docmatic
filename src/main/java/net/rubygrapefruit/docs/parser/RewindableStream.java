package net.rubygrapefruit.docs.parser;

/**
 * A {@code RewindableStream} maintains both a current cursor position and a mark position, and allows the cursor to be
 * moved back to the mark.
 */
public interface RewindableStream {
    /**
     * Moves the cursor back to the mark. Does not affect the mark.
     */
    void rewind();

    /**
     * Moves the mark to the current cursor position.
     */
    void accept();
}
