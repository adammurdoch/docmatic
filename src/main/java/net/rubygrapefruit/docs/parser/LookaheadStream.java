package net.rubygrapefruit.docs.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class LookaheadStream<T> {
    private final List<T> queue = new ArrayList<T>();

    protected LookaheadStream() {
    }

    protected LookaheadStream(Iterable<? extends T> initialValues) {
        for (T initialValue : initialValues) {
            queue.add(initialValue);
        }
    }

    public T peek() {
        return peek(0);
    }

    public T peek(int depth) {
        while (queue.size() <= depth) {
            int size = queue.size();
            readNext(queue);
            if (queue.size() == size) {
                return endOfStream();
            }
        }
        return queue.get(depth);
    }

    public T next() {
        peek(0);
        if (!queue.isEmpty()) {
            return queue.remove(0);
        }
        return null;
    }

    /**
     * Returns the element that marks end of stream.
     */
    protected T endOfStream() {
        return null;
    }

    /**
     * Returns the next available elements.
     */
    protected abstract void readNext(Collection<T> elements);
}
