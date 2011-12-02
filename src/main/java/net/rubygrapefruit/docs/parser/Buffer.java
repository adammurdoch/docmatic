package net.rubygrapefruit.docs.parser;

import java.io.IOException;
import java.io.Reader;

public class Buffer {
    private static final Reader EMPTY_READER = new Reader() {
        @Override
        public int read(char[] chars, int i, int i1) throws IOException {
            return -1;
        }

        @Override
        public void close() throws IOException {
        }
    };
    private final Reader reader;
    private final char[] buffer;
    int startToken = 0;
    int endToken = 0;
    int endBuffer = 0;
    boolean matched;

    public Buffer(Reader reader) {
        this.reader = reader;
        buffer = new char[8192];
    }

    public Buffer(CharSequence source) {
        buffer = new char[source.length() + 1];
        for (int i = 0; i < buffer.length - 1; i++) {
            buffer[i] = source.charAt(i);
        }
        endBuffer = buffer.length - 1;
        reader = EMPTY_READER;
    }

    public boolean scanFor(TokenSpec spec) {
        startToken = endToken;
        matched = false;
        spec.match(this);
        return matched;
    }

    public String getValue() {
        return new String(buffer, startToken, endToken - startToken);
    }

    public void unwind() {
        endToken = startToken;
        matched = false;
    }

    public boolean lookingAt(char... candidates) {
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
            System.arraycopy(buffer, startToken, buffer, 0, endBuffer - startToken);
            endToken -= startToken;
            endBuffer -= startToken;
            startToken = 0;
            int nread = 0;
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
        matched = true;
    }
}
