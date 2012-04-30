package net.rubygrapefruit.docs.parser;

public interface MarkableStream extends RewindableStream {
    /**
     * Places a mark at the current cursor position.
     */
    void start();

    /**
     * Removes the most recent mark, without affecting the cursor position.
     *
     * @return true if the cursor position > mark position, false if the cursor and mark are the same.
     */
    boolean commit();

    /**
     * Moves the cursor back to the most recent mark, and removes the mark.
     */
    void rollback();
}
