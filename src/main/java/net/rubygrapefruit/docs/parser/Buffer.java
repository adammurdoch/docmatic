package net.rubygrapefruit.docs.parser;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class Buffer implements CharStream, MarkableStream {
    private final Reader reader;
    private final char[] buffer;
    private final List<Mark> marks = new ArrayList<Mark>();
    private int firstMark = 0;
    private int cursor = 0;
    private int currentLine = 1;
    private int currentCol = 1;
    private int endBuffer = 0;
    private int startProduction = 0;
    private int endProduction = 0;
    private int startLine = 0;
    private int startColumn = 0;
    private int endLine = 0;
    private int endColumn = 0;

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

    public int getEndColumn() {
        return endColumn;
    }

    public int getStartColumn() {
        return startColumn;
    }

    public int getStartLine() {
        return startLine;
    }

    public int getEndLine() {
        return endLine;
    }

    public void start() {
        if (marks.isEmpty()) {
            firstMark = cursor;
        }
        marks.add(new Mark(cursor, currentLine, currentCol));
        startProduction = -1;
        endProduction = -1;
        startColumn = -1;
        startLine = -1;
        endColumn = -1;
        endLine = -1;
    }

    public boolean commit() {
        Mark mark = marks.remove(marks.size() - 1);
        int startThisToken = mark.offset;
        startLine = mark.line;
        startColumn = mark.col;
        startProduction = startThisToken;
        endProduction = cursor;
        endLine = currentLine;
        endColumn = currentCol - 1;
        return cursor > startThisToken;
    }

    public void rollback() {
        unwind();
        marks.remove(marks.size() - 1);
    }

    public void unwind() {
        cursor = marks.get(marks.size() - 1).offset;
    }

    public boolean consume(Production<? super CharStream> production) {
        start();
        production.match(this);
        return commit();
    }

    public <T> T consume(ValueProducingProduction<? super CharStream, T> production) {
        start();
        T value = production.match(this);
        if (value == null) {
            rollback();
        } else {
            commit();
        }
        return value;
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
                marks.get(i).move(firstMark);
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
        boolean hasCr = false;
        if (buffer[cursor] == '\n') {
            currentCol = 1;
            currentLine++;
        } else if (buffer[cursor] == '\r') {
            hasCr = true;
        } else {
            currentCol++;
        }

        cursor++;

        if (hasCr) {
            if (peek() != '\n') {
                currentCol = 1;
                currentLine++;
            }
        }
    }

    private static class Mark {
        int offset;
        final int col;
        final int line;

        private Mark(int offset, int line, int col) {
            this.offset = offset;
            this.line = line;
            this.col = col;
        }

        public void move(int offset) {
            this.offset -= offset;
        }
    }
}
