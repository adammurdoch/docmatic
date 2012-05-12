package net.rubygrapefruit.docs.markdown;

import net.rubygrapefruit.docs.model.buildable.*;
import net.rubygrapefruit.docs.parser.*;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Builds a document for some Markdown input.
 */
public class MarkdownParser extends Parser {
    @Override
    protected void doParse(Reader input, String fileName, BuildableDocument document) throws Exception {
        Lexer lexer = new Lexer(input);
        LineParser parser = new LineParser(lexer);
        while (parser.peek().type != LineType.Finish) {
            if (empty(parser)) {
                continue;
            }
            if (header1(parser, document)) {
                continue;
            }
            if (header2(parser, document)) {
                continue;
            }
            if (itemisedList(parser, document.getCurrent())) {
                continue;
            }
            if (orderedList(parser, document.getCurrent())) {
                continue;
            }
            if (para(parser, document.getCurrent())) {
                continue;
            }

            throw new UnsupportedOperationException(String.format("Did not match any productions."));
        }
    }

    private boolean para(LookaheadStream<Line> parser, BuildableBlockContainer container) throws IOException {
        BuildableParagraph paragraph = container.addParagraph();
        ParagraphParser paragraphParser = new ParagraphParser(parser);
        inline(paragraphParser, paragraph);
        return true;
    }

    private boolean itemisedList(LookaheadStream<Line> parser, BuildableBlockContainer container) throws IOException {
        if (parser.peek().type != LineType.ItemisedListItem) {
            return false;
        }

        BuildableList list = container.addItemisedList();
        while (listItem(parser, list, LineType.ItemisedListItem)) {
        }

        return true;
    }

    private boolean orderedList(LookaheadStream<Line> parser, BuildableBlockContainer container) throws IOException {
        if (parser.peek().type != LineType.OrderedListItem) {
            return false;
        }

        BuildableList list = container.addOrderedList();
        while (listItem(parser, list, LineType.OrderedListItem)) {
        }

        return true;
    }

    private boolean listItem(LookaheadStream<Line> parser, BuildableList list, LineType itemType) throws IOException {
        if (parser.peek().type != itemType) {
            return false;
        }

        BuildableListItem item = list.addItem();
        para(new ListItemContentParser(parser), item);
        while (true) {
            if (parser.peek().type != LineType.Empty) {
                break;
            }
            while (parser.peek().type == LineType.Empty) {
                parser.next();
            }
            if (parser.peek().type == LineType.Continue) {
                para(new ListItemContentParser(parser), item);
            }
        }

        return true;
    }

    private boolean empty(LookaheadStream<Line> parser) throws IOException {
        Line line = parser.peek();
        if (line.type == LineType.Empty) {
            parser.next();
            return true;
        }
        return false;
    }

    private boolean header1(LookaheadStream<Line> parser, BuildableComponent component) throws IOException {
        Line line1 = parser.peek();
        Line line2 = parser.peek(1);
        if (line2.type == LineType.H1) {
            parser.next();
            parser.next();
            inline(new FixedTokens(line1.tokens), component.addSection(1).getTitle());
            return true;
        }
        return false;
    }

    private boolean header2(LookaheadStream<Line> parser, BuildableComponent component) throws IOException {
        Line line1 = parser.peek();
        Line line2 = parser.peek(1);
        if (line2.type == LineType.H2) {
            parser.next();
            parser.next();
            inline(new FixedTokens(line1.tokens), component.addSection(2).getTitle());
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
            BuildableInlineContainer container = new BuildableInlineContainer();
            appendContent(container);
            return String.format("{%s '%s'}", type, container.getText());
        }

        void appendContent(BuildableInlineContainer dest) {
            appendContent(0, dest);
        }

        void appendContent(Collection<Token> dest) {
            dest.addAll(tokens);
        }

        void appendContent(int startToken, BuildableInlineContainer dest) {
            for (int i = startToken; i < tokens.size(); i++) {
                Token token = tokens.get(i);
                dest.append(token.value);
            }
        }
    }

    private void inline(LookaheadStream<Token> parser, BuildableInlineContainer container) {
        while (parser.peek() != null) {
            if (code(parser, container)) {
                continue;
            }
            if (emphasis(parser, container)) {
                continue;
            }
            container.append(parser.next().value);
        }
    }

    private boolean emphasis(LookaheadStream<Token> parser, BuildableInlineContainer container) {
        Token start = parser.peek();
        if (start.type != Lexer.underscore && start.type != Lexer.star) {
            return false;
        }
        Token next = parser.peek(1);
        if (next == null || next.type == start.type || next.type == Lexer.whiteSpace) {
            return false;
        }
        int pos = 2;
        while (parser.peek(pos) != null) {
            Token token = parser.peek(pos);
            if (token.type == start.type && token.value.equals(start.value)) {
                BuildableEmphasis emphasis = container.addEmphasis();

                parser.next();
                while (pos > 1) {
                    emphasis.append(parser.next().value);
                    pos--;
                }
                parser.next();
                return true;
            }

            pos++;
        }

        return false;
    }

    private boolean code(LookaheadStream<Token> parser, BuildableInlineContainer container) {
        if (parser.peek().type != Lexer.backtick) {
            return false;
        }

        int depth = parser.peek().value.length();
        int pos = 1;
        while (parser.peek(pos) != null) {
            Token token = parser.peek(pos);
            if (token.type == Lexer.backtick && token.value.length() == depth) {
                BuildableCode code = container.addCode();

                parser.next();
                while (pos > 1) {
                    code.append(parser.next().value);
                    pos--;
                }
                parser.next();
                return true;
            }
            pos++;
        }

        return false;
    }

    private static class FixedTokens extends LookaheadStream<Token> {
        private FixedTokens(Iterable<? extends Token> initialValues) {
            super(initialValues);
        }

        @Override
        protected void readNext(Collection<Token> elements) {
        }
    }

    private static class ParagraphParser extends LookaheadStream<Token> {
        private final LookaheadStream<Line> parser;
        private boolean empty = true;

        public ParagraphParser(LookaheadStream<Line> parser) {
            this.parser = parser;
        }

        @Override
        protected void readNext(Collection<Token> tokens) {
            if (parser.peek().type == LineType.Finish || parser.peek().type == LineType.Empty) {
                return;
            }
            if (!empty) {
                tokens.add(new Token(Lexer.whiteSpace, " "));
            } else {
                empty = false;
            }
            parser.next().appendContent(tokens);
        }
    }

    private static class ListItemContentParser extends LookaheadStream<Line> {
        final LookaheadStream<Line> parser;
        boolean empty = true;

        private ListItemContentParser(LookaheadStream<Line> parser) {
            this.parser = parser;
        }

        @Override
        protected Line endOfStream() {
            return new Line(LineType.Finish, Collections.<Token>emptyList());
        }

        @Override
        protected void readNext(Collection<Line> elements) {
            Line line = parser.peek();
            if (line.type == LineType.Finish) {
                return;
            }
            if (line.type == LineType.ItemisedListItem || line.type == LineType.OrderedListItem) {
                if (!empty) {
                    return;
                }
                parser.next();
                elements.add(new Line(LineType.Text, line.tokens.subList(2, line.tokens.size())));
                empty = false;
                return;
            }

            if (line.type == LineType.Empty) {
                return;
            }

            empty = false;
            parser.next();
            elements.add(line);
        }
    }

    private static class LineParser extends LookaheadStream<Line> {
        final Lexer lexer;

        private LineParser(Lexer lexer) {
            this.lexer = lexer;
        }

        @Override
        protected void readNext(Collection<Line> elements) {
            elements.add(readNext());
        }

        protected Line readNext() {
            List<Token> tokens = new ArrayList<Token>();
            boolean hasContent = false;
            while (lexer.next()) {
                hasContent = true;
                if (lexer.getType() == Lexer.endOfLine) {
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
            if (tokens.size() == 1 && tokens.get(0).type == Lexer.equalsToken) {
                return new Line(LineType.H1, tokens);
            }
            if (tokens.size() == 1 && tokens.get(0).type == Lexer.dashes) {
                return new Line(LineType.H2, tokens);
            }
            if (tokens.size() > 2 && tokens.get(0).type == Lexer.plus && tokens.get(1).type
                    == Lexer.whiteSpace) {
                return new Line(LineType.ItemisedListItem, tokens);
            }
            if (tokens.size() > 2 && tokens.get(0).type == Lexer.star && tokens.get(1).type
                    == Lexer.whiteSpace) {
                return new Line(LineType.ItemisedListItem, tokens);
            }
            if (tokens.size() > 2 && tokens.get(0).type == Lexer.dashes && tokens.get(0).value.length() == 1
                    && tokens.get(1).type == Lexer.whiteSpace) {
                return new Line(LineType.ItemisedListItem, tokens);
            }
            if (tokens.size() > 2 && tokens.get(0).type == Lexer.numberedListItem && tokens.get(1).type
                    == Lexer.whiteSpace) {
                return new Line(LineType.OrderedListItem, tokens);
            }
            return new Line(LineType.Text, tokens);
        }
    }

    private static class Token {
        private final Production<CharStream> type;
        private final String value;

        private Token(Production<CharStream> type, String value) {
            this.type = type;
            this.value = value;
        }
    }

    private static class Lexer {
        static final EndOfLineProduction endOfLine = new EndOfLineProduction();
        static final WordProduction word = new WordProduction();
        static final Production<CharStream> whiteSpace = Productions.matchAtLeastOneOf(' ', '\t');
        static final Production<CharStream> equalsToken = Productions.matchAtLeastOneOf('=');
        static final Production<CharStream> dashes = Productions.matchAtLeastOneOf('-');
        static final Production<CharStream> plus = Productions.match('+');
        static final Production<CharStream> dash = Productions.match('-');
        static final Production<CharStream> backtick = Productions.matchAtLeastOneOf('`');
        static final Production<CharStream> underscore = Productions.match('_');
        static final Production<CharStream> star = Productions.match('*');
        static final Production<CharStream> numberedListItem = new NumberedItemProduction();

        private final Buffer buffer;
        private boolean atStartOfLine;
        private Production<CharStream> type;

        private Lexer(Reader input) {
            this.buffer = new Buffer(input);
        }

        Token getToken() {
            return new Token(type, buffer.getValue());
        }

        Production<CharStream> getType() {
            return type;
        }

        boolean next() {
            type = scanNext();
            atStartOfLine = (type == endOfLine);
            return type != null;
        }

        Production<CharStream> scanNext() {
            if (buffer.consume(endOfLine)) {
                return endOfLine;
            }
            if (atStartOfLine && buffer.consume(numberedListItem)) {
                return numberedListItem;
            }
            if (atStartOfLine && buffer.consume(equalsToken)) {
                return equalsToken;
            }
            if (buffer.consume(plus)) {
                return plus;
            }
            if (buffer.consume(dashes)) {
                return dashes;
            }
            if (buffer.consume(backtick)) {
                return backtick;
            }
            if (buffer.consume(underscore)) {
                return underscore;
            }
            if (buffer.consume(star)) {
                return star;
            }
            if (buffer.consume(whiteSpace)) {
                return whiteSpace;
            }
            if (buffer.consume(word)) {
                return word;
            }
            return null;
        }
    }

    private static class EndOfLineProduction implements Production<CharStream> {
        public void match(CharStream charStream) {
            charStream.consume('\r');
            charStream.consume('\n');
        }
    }

    private static class NumberedItemProduction implements Production<CharStream> {
        private final Production<CharStream> digits = Productions.matchAtLeastOnce(Productions.matchFromRange('0', '9'));

        public void match(CharStream charStream) {
            if (!charStream.consume(digits)) {
                return;
            }
            if (!charStream.consume('.')) {
                charStream.rewind();
            }
        }
    }

    private static class WordProduction implements Production<CharStream> {
        public void match(CharStream charStream) {
            while (charStream.consumeAnyExcept(' ', '\t', '\r', '\n', '`', '_', '*')) {
            }
        }
    }
}
