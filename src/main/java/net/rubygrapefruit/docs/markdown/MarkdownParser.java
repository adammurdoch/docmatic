package net.rubygrapefruit.docs.markdown;

import net.rubygrapefruit.docs.model.BuildableDocument;
import net.rubygrapefruit.docs.model.Document;
import net.rubygrapefruit.docs.parser.Parser;

import java.io.*;

/**
 * Builds a document for some Markdown input.
 */
public class MarkdownParser extends Parser {
    @Override
    public Document parse(Reader input) {
        try {
            BuildableDocument document = new BuildableDocument();
            Lexer lexer = new Lexer(input);
            StringBuilder currentPara = new StringBuilder();
            boolean currentLineHasText = false;
            boolean currentParaHasText = false;
            while (lexer.next()) {
                switch (lexer.getToken()) {
                    case Word:
                        currentPara.append(lexer.getValue());
                        currentParaHasText = true;
                        currentLineHasText = true;
                        break;
                    case WhiteSpace:
                        currentPara.append(lexer.getValue());
                        break;
                    case EOL:
                        if (!currentLineHasText) {
                            if (currentParaHasText) {
                                document.addParagraph().setText(currentPara.toString());
                            }
                            currentParaHasText = false;
                            currentLineHasText = false;
                            currentPara.setLength(0);
                        } else {
                            currentPara.append(lexer.getValue());
                            currentLineHasText = false;
                        }
                        break;
                }
            }
            if (currentParaHasText) {
                document.addParagraph().setText(currentPara.toString());
            }
            return document;
        } catch (IOException e) {
            throw new RuntimeException("Could not parse markdown document.", e);
        }
    }

    private static class Lexer {

        enum Token {
            WhiteSpace, Word, EOL, EOT
        }

        final Buffer buffer;
        Token token;

        private Lexer(Reader input) {
            this.buffer = new Buffer(input);
        }

        boolean next() throws IOException {
            if (buffer.scanFor(new EndOfLineSpec())) {
                token = Token.EOL;
                return true;
            }
            if (buffer.scanFor(new WordSpec())) {
                token = Token.Word;
                return true;
            }
            if (buffer.scanFor(new WhiteSpaceSpec())) {
                token = Token.WhiteSpace;
                return true;
            }
            token = Token.EOT;
            return false;
        }

        public Token getToken() {
            return token;
        }

        public String getValue() {
            return buffer.getValue();
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

    private static class WhiteSpaceSpec extends TokenSpec {
        @Override
        public void match(Buffer buffer) throws IOException {
            while (buffer.moveOver(' ', '\t')) {
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
