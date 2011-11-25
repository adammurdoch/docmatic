package net.rubygrapefruit.docs.markdown;

import net.rubygrapefruit.docs.model.*;
import net.rubygrapefruit.docs.parser.Buffer;
import net.rubygrapefruit.docs.parser.Parser;
import net.rubygrapefruit.docs.parser.TokenSpec;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds a document for some Markdown input.
 */
public class MarkdownParser extends Parser {
    @Override
    protected Document doParse(Reader input, String fileName) throws Exception {
        BuildableDocument document = new BuildableDocument();
        DocumentBuilder builder = new DocumentBuilder(document);
        Lexer lexer = new Lexer(input);
        LineParser parser = new LineParser(lexer);
        while (parser.peek().type != LineType.Finish) {
            if (empty(parser)) {
                continue;
            }
            if (header1(parser, builder)) {
                continue;
            }
            if (header2(parser, builder)) {
                continue;
            }
            if (itemisedList(parser, builder.getCurrentComponent())) {
                continue;
            }
            if (orderedList(parser, builder.getCurrentComponent())) {
                continue;
            }
            if (para(parser, builder.getCurrentComponent())) {
                continue;
            }

            throw new UnsupportedOperationException(String.format("Did not match any productions."));
        }

        return document;
    }

    private boolean para(LineParser parser, BuildableBlockContainer container) throws IOException {
        BuildableParagraph paragraph = container.addParagraph();
        paragraph.append(parser.next().getContent());
        while (parser.peek().type != LineType.Finish && parser.peek().type != LineType.Empty) {
            paragraph.append(" ");
            paragraph.append(parser.next().getContent());
        }
        return true;
    }

    private boolean itemisedList(LineParser parser, BuildableBlockContainer container) throws IOException {
        if (parser.peek().type != LineType.ItemisedListItem) {
            return false;
        }

        BuildableList list = container.addItemisedList();
        while (listItem(parser, list, LineType.ItemisedListItem)) {
        }

        return true;
    }

    private boolean orderedList(LineParser parser, BuildableBlockContainer container) throws IOException {
        if (parser.peek().type != LineType.OrderedListItem) {
            return false;
        }

        BuildableList list = container.addOrderedList();
        while (listItem(parser, list, LineType.OrderedListItem)) {
        }

        return true;
    }

    private boolean listItem(LineParser parser, BuildableList list, LineType itemType) throws IOException {
        if (parser.peek().type != itemType) {
            return false;
        }

        Line line = parser.next();
        BuildableListItem item = list.addItem();
        BuildableParagraph paragraph = item.addParagraph();
        paragraph.append(line.getContent(2));

        while (parser.peek().type != LineType.Finish && parser.peek().type != itemType) {
            while (parser.peek().type != LineType.Finish && parser.peek().type != LineType.Empty
                    && parser.peek().type != itemType) {
                line = parser.next();
                paragraph.append(" ");
                paragraph.append(line.getContent());
            }

            while (parser.peek().type == LineType.Empty) {
                parser.next();
            }

            if (parser.peek().type != LineType.Continue) {
                break;
            }

            paragraph = item.addParagraph();
            paragraph.append(parser.next().getContent(1));
        }

        return true;
    }

    private boolean empty(LineParser parser) throws IOException {
        Line line = parser.peek();
        if (line.type == LineType.Empty) {
            parser.next();
            return true;
        }
        return false;
    }

    private boolean header1(LineParser parser, DocumentBuilder builder) throws IOException {
        Line line1 = parser.peek();
        Line line2 = parser.peek(1);
        if (line2.type == LineType.H1) {
            parser.next();
            parser.next();
            builder.appendSection(1).getTitle().append(line1.getContent());
            return true;
        }
        return false;
    }

    private boolean header2(LineParser parser, DocumentBuilder builder) throws IOException {
        Line line1 = parser.peek();
        Line line2 = parser.peek(1);
        if (line2.type == LineType.H2) {
            parser.next();
            parser.next();
            builder.appendSection(2).getTitle().append(line1.getContent());
            return true;
        }
        return false;
    }

    enum LineType {
        Empty, Text, H1, H2, ItemisedListItem, OrderedListItem, Finish, Continue
    }

    private static class Line {
        final LineType type;
        final List<Token> tokens;

        private Line(LineType type, List<Token> tokens) {
            this.type = type;
            this.tokens = tokens;
        }

        @Override
        public String toString() {
            return String.format("{%s '%s'}", type, getContent());
        }

        CharSequence getContent() {
            return getContent(0);
        }

        CharSequence getContent(int startToken) {
            StringBuilder builder = new StringBuilder();
            int pos = startToken;
            if (tokens.get(pos).type == Lexer.whiteSpace) {
                pos++;
            }
            for (int i = pos; i < tokens.size(); i++) {
                Token token = tokens.get(i);
                if (token.type == Lexer.whiteSpace) {
                    builder.append(' ');
                } else {
                    builder.append(token.value);
                }
            }
            return builder.toString();
        }
    }

    private static class LineParser {
        final List<Line> queue = new ArrayList<Line>();
        final Lexer lexer;

        private LineParser(Lexer lexer) {
            this.lexer = lexer;
        }

        public Line peek() throws IOException {
            return peek(0);
        }

        public Line peek(int depth) throws IOException {
            while (queue.size() <= depth) {
                Line line = scanLine();
                if (line == null) {
                    return null;
                }
                queue.add(line);
            }
            return queue.get(depth);
        }

        public Line next() throws IOException {
            if (!queue.isEmpty()) {
                return queue.remove(0);
            }
            return scanLine();
        }

        private Line scanLine() throws IOException {
            List<Token> tokens = new ArrayList<Token>();
            boolean hasContent = false;
            while (lexer.next()) {
                hasContent = true;
                if (lexer.getType() == Lexer.endOfLineSpec) {
                    break;
                }
                tokens.add(lexer.getToken());
            }
            if (!hasContent) {
                return new Line(LineType.Finish, tokens);
            }

            if (tokens.size() > 0 && tokens.get(tokens.size() - 1).type == Lexer.whiteSpace) {
                tokens.remove(tokens.size() - 1);
            }
            if (tokens.size() > 1 && tokens.get(0).type == Lexer.whiteSpace) {
                if (tokens.get(0).value.startsWith("    ") || tokens.get(0).value.startsWith("\t")) {
                    return new Line(LineType.Continue, tokens);
                }
            }
            if (tokens.size() > 0 && tokens.get(0).type == Lexer.whiteSpace) {
                tokens.remove(0);
            }

            if (tokens.isEmpty()) {
                return new Line(LineType.Empty, tokens);
            }
            if (tokens.size() > 2 && tokens.get(0).type == Lexer.itemisedListItem && tokens.get(1).type
                    == Lexer.whiteSpace) {
                return new Line(LineType.ItemisedListItem, tokens);
            }
            if (tokens.size() > 2 && tokens.get(0).type == Lexer.numberedListItem && tokens.get(1).type
                    == Lexer.whiteSpace) {
                return new Line(LineType.OrderedListItem, tokens);
            }
            if (tokens.size() == 1 && tokens.get(0).type == Lexer.equalsSpec) {
                return new Line(LineType.H1, tokens);
            }
            if (tokens.size() == 1 && tokens.get(0).type == Lexer.dashes) {
                return new Line(LineType.H2, tokens);
            }
            return new Line(LineType.Text, tokens);
        }
    }

    private static class Token {
        private final TokenSpec type;
        private final String value;

        private Token(TokenSpec type, String value) {
            this.type = type;
            this.value = value;
        }
    }

    private static class Lexer {
        static final EndOfLineSpec endOfLineSpec = new EndOfLineSpec();
        static final WordSpec word = new WordSpec();
        static final TokenSpec whiteSpace = new SequenceSpec(' ', '\t');
        static final TokenSpec equalsSpec = new SequenceSpec('=');
        static final TokenSpec dashes = new SequenceSpec('-');
        static final TokenSpec itemisedListItem = new ChoiceSpec('*', '+', '-');
        static final TokenSpec numberedListItem = new NumberedItemSpec();

        private final Buffer buffer;
        private boolean atStartOfLine;
        private TokenSpec type;

        private Lexer(Reader input) {
            this.buffer = new Buffer(input);
        }

        Token getToken() {
            return new Token(type, buffer.getValue());
        }

        TokenSpec getType() {
            return type;
        }

        boolean next() throws IOException {
            type = scanNext();
            atStartOfLine = (type == endOfLineSpec);
            return type != null;
        }

        TokenSpec scanNext() throws IOException {
            if (buffer.scanFor(endOfLineSpec)) {
                return endOfLineSpec;
            }
            if (atStartOfLine && buffer.scanFor(itemisedListItem)) {
                return itemisedListItem;
            }
            if (atStartOfLine && buffer.scanFor(numberedListItem)) {
                return numberedListItem;
            }
            if (atStartOfLine && buffer.scanFor(equalsSpec)) {
                return equalsSpec;
            }
            if (atStartOfLine && buffer.scanFor(dashes)) {
                return dashes;
            }
            if (buffer.scanFor(whiteSpace)) {
                return whiteSpace;
            }
            if (buffer.scanFor(word)) {
                return word;
            }
            return null;
        }
    }

    private static class EndOfLineSpec implements TokenSpec {
        public void match(Buffer buffer) throws IOException {
            buffer.consume('\r');
            buffer.consume('\n');
        }
    }

    private static class SequenceSpec implements TokenSpec {
        private final char[] candidates;

        private SequenceSpec(char... candidates) {
            this.candidates = candidates;
        }

        public void match(Buffer buffer) throws IOException {
            while (buffer.consume(candidates)) {
            }
        }
    }

    private static class ChoiceSpec implements TokenSpec {
        private final char[] candidates;

        private ChoiceSpec(char... candidates) {
            this.candidates = candidates;
        }

        public void match(Buffer buffer) throws IOException {
            for (char candidate : candidates) {
                if (buffer.consume(candidate)) {
                    if (!buffer.lookingAt(candidate)) {
                        return;
                    }
                    buffer.unwind();
                }
            }
        }
    }

    private static class NumberedItemSpec implements TokenSpec {
        public void match(Buffer buffer) throws IOException {
            if (!buffer.consumeRange('0', '9')) {
                return;
            }
            while (buffer.consumeRange('0', '9')) {
                ;
            }
            if (!buffer.consume('.')) {
                buffer.unwind();
            }
        }
    }

    private static class WordSpec implements TokenSpec {
        public void match(Buffer buffer) throws IOException {
            while (buffer.consumeAnyExcept(' ', '\t', '\r', '\n')) {
            }
        }
    }
}
