package net.rubygrapefruit.docs.parser;

public interface MarkableStream extends RewindableStream {
    /**
     * Pushes a mark at the current cursor position.
     */
    void start();

    /**
     * Pops the most recent mark, without affecting the cursor position.
     *
     * @return true if the cursor position has moved beyond the mark position, false if the cursor and mark are the same
     *         position.
     */
    boolean commit();

    /**
     * Moves the cursor back to the most recently pushed mark, and pops the mark.
     */
    void rollback();
}
