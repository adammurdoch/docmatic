package net.rubygrapefruit.docs.parser;

import java.util.ArrayList;
import java.util.List;

public abstract class LookaheadStream<T> {
    private final List<T> queue = new ArrayList<T>();

    public T peek() {
        return peek(0);
    }

    public T peek(int depth) {
        while (queue.size() <= depth) {
            T next = readNext();
            if (next == null) {
                return null;
            }
            queue.add(next);
        }
        return queue.get(depth);
    }

    public T next() {
        if (!queue.isEmpty()) {
            return queue.remove(0);
        }
        return readNext();
    }

    /**
     * Returns the next element, or null at the end of the stream.
     */
    protected abstract T readNext();
}
