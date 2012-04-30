package net.rubygrapefruit.docs.parser;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class Buffer implements CharStream, MarkableStream {
    private final Reader reader;
    private final char[] buffer;
    private final List<Integer> marks = new ArrayList<Integer>();
    private int firstMark = 0;
    private int cursor = 0;
    private int endBuffer = 0;
    private int startProduction = 0;
    private int endProduction = 0;

    public Buffer(Reader reader) {
        this(reader, 8192);
    }

    public Buffer(Reader reader, int bufferLen) {
        this.reader = reader;
        buffer = new char[bufferLen];
    }

    public String getValue() {
        return new String(buffer, startProduction, endProduction - startProduction);
    }

    public void start() {
        if (marks.isEmpty()) {
            firstMark = cursor;
        }
        marks.add(cursor);
    }

    public boolean commit() {
        int startThisToken = marks.remove(marks.size() - 1);
        startProduction = startThisToken;
        endProduction = cursor;
        return cursor > startThisToken;
    }

    public void rollback() {
        unwind();
        marks.remove(marks.size() - 1);
    }

    public void unwind() {
        cursor = marks.get(marks.size() - 1);
    }

    public boolean consume(Production<? super CharStream> production) {
        start();
        production.match(this);
        return commit();
    }

    public boolean consumeAtLeastOne(Production<? super CharStream> production) {
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
        if (cursor == endBuffer) {
            if (firstMark == 0 && endBuffer == buffer.length) {
                throw new UnsupportedOperationException("Buffer overflow not implemented yet.");
            }

            // Move any consumed characters to the start of the buffer
            System.arraycopy(buffer, firstMark, buffer, 0, endBuffer - firstMark);
            cursor -= firstMark;
            endBuffer -= firstMark;
            for (int i = 0; i < marks.size(); i++) {
                marks.set(i, marks.get(i) - firstMark);
            }
            firstMark = 0;

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

        return buffer[cursor];
    }

    private void next() {
        cursor++;
    }
}
