package net.rubygrapefruit.docs.html;

import net.rubygrapefruit.docs.model.buildable.BuildableDocument;
import net.rubygrapefruit.docs.model.buildable.BuildableParagraph;
import net.rubygrapefruit.docs.parser.*;

import java.io.Reader;

/**
 * doc = element element = empty-element | element-with-content empty-element = '<' element-name '/>'
 * element-with-content = start-element (element | text | cdata)* end-element start-element = '<' element-name '>'
 * end-element = '</' element-name '>' text = (!reserved-char | entity)+ cdata = ??
 */
public class HtmlParser extends Parser {
    private final Name name = new Name();
    private final Production<CharStream> whitespace = Productions.matchAtLeastOneOf(' ', '\t', '\r', '\n', '\f');
    private final Comment comment = new Comment();
    private final Production<CharStream> ignorableContent = Productions.matchFirstOf(whitespace, comment);
    private final StartTag startTag = new StartTag();
    private final EndTag endTag = new EndTag();
    private final TextProduction textProduction = new TextProduction();

    @Override
    protected void doParse(Reader input, String fileName, BuildableDocument document) throws Exception {
        LenientHtmlDocument documentProduction = new LenientHtmlDocument(document, fileName);
        Buffer buffer = new Buffer(input);
        buffer.consume(documentProduction);
    }

    private static class Token {
        private final String value;
        private final int line;
        private final int col;

        private Token(String value, int line, int col) {
            this.value = value;
            this.line = line;
            this.col = col;
        }
    }

    private class LenientHtmlDocument implements Production<CharStream> {
        private final BuildableDocument document;
        private final String fileName;

        private LenientHtmlDocument(BuildableDocument document, String fileName) {
            this.document = document;
            this.fileName = fileName;
        }

        public void match(CharStream stream) {
            if (stream.consume(new HtmlDocument(document, fileName))) {
            } else {
                stream.consume(new BodyBody(document));
            }
        }
    }

    private class HtmlDocument implements Production<CharStream> {
        private final BuildableDocument document;
        private final String fileName;

        private HtmlDocument(BuildableDocument document, String fileName) {
            this.document = document;
            this.fileName = fileName;
        }

        public void match(CharStream stream) {
            while (stream.consume(ignorableContent)) {
            }
            Token beforeDocType = stream.consume(textProduction);
            boolean hasDocType = stream.consume(new Doctype());
            while (stream.consume(ignorableContent)) {
            }
            Token afterDocType = stream.consume(textProduction);
            boolean hasHtmlElement = stream.consume(new HtmlElement(document));
            if (!hasDocType && !hasHtmlElement) {
                stream.rewind();
                return;
            }
            while (stream.consume(ignorableContent)) {
            }
            Token afterContent = stream.consume(textProduction);
            if (beforeDocType != null) {
                document.addError(String.format("unexpected text in %s, line %s, column %s.", fileName, beforeDocType.line,
                        beforeDocType.col));
            }
            if (afterDocType != null) {
                document.addError(String.format("unexpected text in %s, line %s, column %s.", fileName, afterDocType.line,
                        afterDocType.col));
            }
            if (afterContent != null) {
                document.addError(String.format("unexpected text in %s, line %s, column %s.", fileName, afterContent.line,
                        afterContent.col));
            }
        }
    }

    private abstract class ElementProduction implements Production<CharStream> {
        public void match(CharStream stream) {
            Token token = stream.consume(startTag);
            if (token == null) {
                return;
            }
            if (!token.value.equalsIgnoreCase(getTagName())) {
                stream.rewind();
                return;
            }
            stream.accept();
            stream.consume(getBody());
            // TODO - handle mismatched end tags here
            stream.consume(endTag);
        }

        protected abstract Production<? super CharStream> getBody();

        protected abstract String getTagName();
    }

    private class HtmlElement extends ElementProduction {
        private final BuildableDocument document;

        private HtmlElement(BuildableDocument document) {
            this.document = document;
        }

        @Override
        protected String getTagName() {
            return "html";
        }

        @Override
        protected Production<? super CharStream> getBody() {
            return new HtmlBody(document);
        }
    }

    private class HtmlBody implements Production<CharStream> {
        private final BuildableDocument document;

        private HtmlBody(BuildableDocument document) {
            this.document = document;
        }

        public void match(CharStream stream) {
            while (stream.consume(ignorableContent)) {
            }
            if (stream.consume(new BodyElement(document))) {
            } else {
                stream.consume(new BodyBody(document));
            }
            while (stream.consume(ignorableContent)) {
            }
        }
    }

    private class BodyElement extends ElementProduction {
        private final BuildableDocument document;

        private BodyElement(BuildableDocument document) {
            this.document = document;
        }

        @Override
        protected String getTagName() {
            return "body";
        }

        @Override
        protected Production<? super CharStream> getBody() {
            return new BodyBody(document);
        }
    }

    private class BodyBody implements Production<CharStream> {
        private final BuildableDocument document;

        private BodyBody(BuildableDocument document) {
            this.document = document;
        }

        public void match(CharStream stream) {
            while (true) {
                if (stream.consume(ignorableContent)) {
                    continue;
                }
                if (stream.consume(new ParagraphElement(document))) {
                    continue;
                }
                Token token = stream.consume(textProduction);
                if (token != null) {
                    document.addParagraph().append(token.value);
                    continue;
                }
                break;
            }
        }
    }

    private class ParagraphElement extends ElementProduction {
        private final BuildableDocument document;

        private ParagraphElement(BuildableDocument document) {
            this.document = document;
        }

        @Override
        protected String getTagName() {
            return "p";
        }

        @Override
        protected Production<? super CharStream> getBody() {
            return new ParagraphBody(document.addParagraph());
        }
    }

    private class ParagraphBody implements Production<CharStream> {
        private final BuildableParagraph paragraph;

        private ParagraphBody(BuildableParagraph paragraph) {
            this.paragraph = paragraph;
        }

        public void match(CharStream stream) {
            while (true) {
                if (stream.consume(comment)) {
                    continue;
                }
                Token value = stream.consume(textProduction);
                if (value != null) {
                    paragraph.append(value.value);
                    continue;
                }
                break;
            }
        }
    }

    private static class Name implements Production<CharStream> {
        public void match(CharStream charStream) {
            // TODO - legal element name characters here
            if (!charStream.consumeAnyExcept(' ', '\t', '>', '<', '=', '/')) {
                return;
            }
            while (charStream.consumeAnyExcept(' ', '\t', '>', '<', '=', '/')) {
            }
        }
    }

    private class StartTag implements ValueProducingProduction<CharStream, Token> {
        public Token match(CharStream charStream) {
            if (!charStream.consume('<')) {
                return null;
            }
            // TODO - whitespace here?
            if (!charStream.consume(name)) {
                return null;
            }
            String name = charStream.getValue();
            int line = charStream.getStartLine();
            int col = charStream.getStartColumn();
            charStream.consume(whitespace);
            // TODO - attributes here
            if (!charStream.consume('>')) {
                return null;
            }
            return new Token(name, line, col);
        }
    }

    private class EndTag implements ValueProducingProduction<CharStream, Token> {
        public Token match(CharStream charStream) {
            if (!charStream.consume('<')) {
                return null;
            }
            if (!charStream.consume('/')) {
                return null;
            }
            // TODO - whitespace here?
            if (!charStream.consume(name)) {
                return null;
            }
            String name = charStream.getValue();
            int line = charStream.getStartLine();
            int col = charStream.getStartColumn();
            charStream.consume(whitespace);
            if (!charStream.consume('>')) {
                return null;
            }
            return new Token(name, line, col);
        }
    }

    private static class Comment implements Production<CharStream> {
        private final Production<CharStream> startComment = Productions.match("<!--");
        private final Production<CharStream> endComment = Productions.match("-->");

        public void match(CharStream charStream) {
            if (!charStream.consume(startComment)) {
                return;
            }
            while (!charStream.consume(endComment)) {
                charStream.consumeAnyExcept();
            }
        }
    }

    private static class Doctype implements Production<CharStream> {
        private final Production<CharStream> startComment = Productions.matchIgnoreCase("<!doctype");
        private final Production<CharStream> type = Productions.matchIgnoreCase("html");
        private final Production<CharStream> endComment = Productions.match(">");

        public void match(CharStream charStream) {
            if (!charStream.consume(startComment)) {
                return;
            }
            if (!charStream.consume(' ')) {
                charStream.rewind();
                return;
            }
            while (charStream.consume(' ')) {
            }
            if (!charStream.consume(type)) {
                charStream.rewind();
                return;
            }
            while (charStream.consume(' ')) {
            }
            if (!charStream.consume(endComment)) {
                charStream.rewind();
                return;
            }
        }
    }

    private static class Text implements Production<CharStream> {
        public void match(CharStream charStream) {
            // TODO - legal text characters here
            while (charStream.consumeAnyExcept('<')) {
            }
        }
    }

    private static class TextProduction implements ValueProducingProduction<CharStream, Token> {
        final Text text = new Text();

        public Token match(CharStream charStream) {
            if (!charStream.consume(text)) {
                return null;
            }
            int line = charStream.getStartLine();
            int col = charStream.getStartColumn();
            String value = charStream.getValue();
            return new Token(value, line, col);
        }
    }
}
