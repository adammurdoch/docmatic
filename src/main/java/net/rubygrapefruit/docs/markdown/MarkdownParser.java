package net.rubygrapefruit.docs.markdown;

import net.rubygrapefruit.docs.model.*;
import net.rubygrapefruit.docs.parser.Parser;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds a document for some Markdown input.
 */
public class MarkdownParser extends Parser {
    @Override
    public Document parse(Reader input) {
        try {
            BuildableDocument document = new BuildableDocument();
            DocumentBuilder builder = new DocumentBuilder(document);
            Lexer lexer = new Lexer(input);
            List<Line> currentPara = new ArrayList<Line>();
            while (true) {
                Line line = nextLine(lexer);
                if (line == null) {
                    addPara(currentPara, builder);
                    break;
                }
                switch (line.type) {
                    case Empty:
                        addPara(currentPara, builder);
                        currentPara.clear();
                        break;
                    case Text:
                        currentPara.add(line);
                        break;
                    case Equals:
                        if (currentPara.size() == 1) {
                            builder.appendSection(1).setTitle(currentPara.get(0).content);
                            currentPara.clear();
                        } else {
                            currentPara.add(line);
                        }
                        break;
                    case Dashes:
                        if (currentPara.size() == 1) {
                            builder.appendSection(2).setTitle(currentPara.get(0).content);
                            currentPara.clear();
                        } else {
                            currentPara.add(line);
                        }
                        break;
                    default:
                        throw new UnsupportedOperationException(String.format("Got unexpected line type '%s'.",
                                line.type));
                }
            }
            return document;
        } catch (IOException e) {
            throw new RuntimeException("Could not parse markdown document.", e);
        }
    }

    private void addPara(List<Line> lines, DocumentBuilder builder) {
        if (lines.isEmpty()) {
            return;
        }
        BuildableParagraph paragraph = builder.appendParagraph();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) {
                paragraph.append(" ");
            }
            Line line = lines.get(i);
            paragraph.append(line.content);
        }
    }

    private Line nextLine(Lexer lexer) throws IOException {
        LineType lineType = LineType.Empty;
        StringBuilder text = new StringBuilder();
        while (lexer.next()) {
            switch (lexer.token.type) {
                case WhiteSpace:
                    if (lineType == LineType.Empty) {
                        continue;
                    }
                    break;
                case Equals:
                    if (lineType == LineType.Empty) {
                        lineType = LineType.Equals;
                    } else {
                        lineType = LineType.Text;
                    }
                    break;
                case Word:
                    lineType = LineType.Text;
                    break;
                case Dashes:
                    if (lineType == LineType.Empty) {
                        lineType = LineType.Dashes;
                    } else {
                        lineType = LineType.Text;
                    }
                    break;
                case EOL:
                    return new Line(lineType, text);
                default:
                    throw new IllegalStateException(String.format("Unexpected token '%s' found.", lexer.token.value));
            }
            text.append(lexer.token.value);
        }
        if (lineType == LineType.Empty) {
            return null;
        }
        return new Line(lineType, text);
    }

    enum LineType {
        Empty, Text, Equals, Dashes
    }

    private static class Line {
        final LineType type;
        final CharSequence content;

        private Line(LineType type, CharSequence content) {
            this.type = type;
            this.content = content;
        }
    }

    enum TokenType {
        WhiteSpace, Word, Equals, Dashes, EOL
    }

    private static class Token {
        final TokenType type;
        final String value;

        private Token(TokenType type, String value) {
            this.type = type;
            this.value = value;
        }
    }

    private static class Lexer {
        private final EndOfLineSpec endOfLineSpec = new EndOfLineSpec();
        private final WordSpec wordSpec = new WordSpec();
        private final TokenSpec whiteSpaceSpec = new SequenceSpec(' ', '\t');
        private final TokenSpec equalsSpec = new SequenceSpec('=');
        private final TokenSpec dashesSpec = new SequenceSpec('-');

        final Buffer buffer;
        Token token;

        private Lexer(Reader input) {
            this.buffer = new Buffer(input);
        }

        boolean next() throws IOException {
            if (buffer.scanFor(endOfLineSpec)) {
                token = new Token(TokenType.EOL, buffer.getValue());
                return true;
            }
            if (buffer.scanFor(equalsSpec)) {
                token = new Token(TokenType.Equals, buffer.getValue());
                return true;
            }
            if (buffer.scanFor(dashesSpec)) {
                token = new Token(TokenType.Dashes, buffer.getValue());
                return true;
            }
            if (buffer.scanFor(wordSpec)) {
                token = new Token(TokenType.Word, buffer.getValue());
                return true;
            }
            if (buffer.scanFor(whiteSpaceSpec)) {
                token = new Token(TokenType.WhiteSpace, buffer.getValue());
                return true;
            }
            token = null;
            return false;
        }
    }

    private static class TokenSpec {
        public void match(Buffer buffer) throws IOException {
        }
    }

    private static class EndOfLineSpec extends TokenSpec {
        @Override
        public void match(Buffer buffer) throws IOException {
            buffer.moveOver('\r');
            buffer.moveOver('\n');
        }
    }

    private static class SequenceSpec extends TokenSpec {
        private final char[] candidates;

        private SequenceSpec(char... candidates) {
            this.candidates = candidates;
        }

        @Override
        public void match(Buffer buffer) throws IOException {
            while (buffer.moveOver(candidates)) {
            }
        }
    }

    private static class WordSpec extends TokenSpec {
        @Override
        public void match(Buffer buffer) throws IOException {
            while (buffer.moveOverExcept(' ', '\t', '\r', '\n')) {
            }
        }
    }

    private static class Buffer {
        private final Reader reader;
        private final char[] buffer = new char[8192];
        int startToken = 0;
        int endToken = 0;
        int endBuffer = 0;
        boolean matched;

        private Buffer(Reader reader) {
            this.reader = reader;
        }

        public boolean scanFor(TokenSpec spec) throws IOException {
            startToken = endToken;
            matched = false;
            spec.match(this);
            return matched;
        }

        public String getValue() {
            return new String(buffer, startToken, endToken - startToken);
        }

        boolean moveOver(char... candidates) throws IOException {
            int ch = peek();
            if (ch < 0) {
                return false;
            }
            for (int i = 0; i < candidates.length; i++) {
                char candidate = candidates[i];
                if (ch == candidate) {
                    next();
                    return true;
                }
            }
            return false;
        }

        public boolean moveOverExcept(char... candidates) throws IOException {
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

        private int peek() throws IOException {
            if (endToken == endBuffer) {
                if (startToken == 0 && endBuffer == buffer.length) {
                    throw new UnsupportedOperationException("Buffer overflow not implemented yet.");
                }
                System.arraycopy(buffer, startToken, buffer, 0, endBuffer - startToken);
                endToken -= startToken;
                endBuffer -= startToken;
                startToken = 0;
                int nread = reader.read(buffer, endBuffer, buffer.length - endBuffer);
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
}
