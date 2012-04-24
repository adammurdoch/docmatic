package net.rubygrapefruit.docs.parser;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class Buffer implements CharStream {
    private final Reader reader;
    private final char[] buffer;
    private final List<Integer> stack = new ArrayList<Integer>();
    private int startToken = 0;
    private int endToken = 0;
    private int endBuffer = 0;

    public Buffer(Reader reader) {
        this(reader, 8192);
    }

    public Buffer(Reader reader, int bufferLen) {
        this.reader = reader;
        buffer = new char[bufferLen];
    }

    public boolean scanFor(CharProduction production) {
        return consume(production);
    }

    public String getValue() {
        if (!stack.isEmpty()) {
            throw new UnsupportedOperationException("Not implemented");
        }
        return new String(buffer, startToken, endToken - startToken);
    }

    public void unwind() {
        endToken = stack.get(stack.size() - 1);
    }

    public boolean consume(CharProduction production) {
        if (stack.isEmpty()) {
            startToken = endToken;
        }
        stack.add(endToken);
        production.match(this);
        int startThisToken = stack.remove(stack.size() - 1);
        return endToken > startThisToken;
    }

    public boolean consumeAtLeastOne(CharProduction production) {
        if (!consume(production)) {
            return false;
        }
        while (consume(production)) {
            ;
        }
        return true;
    }

    private boolean lookingAt(char... candidates) {
        int ch = peek();
        if (ch < 0) {
            return false;
        }
        for (int i = 0; i < candidates.length; i++) {
            char candidate = candidates[i];
            if (ch == candidate) {
                return true;
            }
        }
        return false;
    }

    public boolean consumeRange(char from, char to) {
        int ch = peek();
        if (ch >= from && ch <= to) {
            next();
            return true;
        }
        return false;
    }

    public boolean consume(char... candidates) {
        if (lookingAt(candidates)) {
            next();
            return true;
        }
        return false;
    }

    public boolean consumeAnyExcept(char... candidates) {
        int ch = peek();
        if (ch < 0) {
            return false;
        }
        for (int i = 0; i < candidates.length; i++) {
            char candidate = candidates[i];
            if (ch == candidate) {
                return false;
            }
        }
        next();
        return true;
    }

    private int peek() {
        if (endToken == endBuffer) {
            if (startToken == 0 && endBuffer == buffer.length) {
                throw new UnsupportedOperationException("Buffer overflow not implemented yet.");
            }

            // Move any consumed characters to the start of the buffer
            System.arraycopy(buffer, startToken, buffer, 0, endBuffer - startToken);
            endToken -= startToken;
            endBuffer -= startToken;
            for (int i = 0; i < stack.size(); i++) {
                stack.set(i, stack.get(i) - startToken);
            }
            startToken = 0;

            // Read the next chunk
            int nread;
            try {
                nread = reader.read(buffer, endBuffer, buffer.length - endBuffer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (nread < 0) {
                return -1;
            }
            endBuffer += nread;
        }

        return buffer[endToken];
    }

    private void next() {
        endToken++;
    }
}
