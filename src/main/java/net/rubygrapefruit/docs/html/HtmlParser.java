package net.rubygrapefruit.docs.html;

import net.rubygrapefruit.docs.model.buildable.BuildableDocument;
import net.rubygrapefruit.docs.parser.*;

import java.io.Reader;

/**
 * doc = element element = empty-element | element-with-content empty-element = '<' element-name '/>'
 * element-with-content = start-element (element | text | cdata)* end-element start-element = '<' element-name '>'
 * end-element = '</' element-name '>' text = (!reserved-char | entity)+ cdata = ??
 */
public class HtmlParser extends Parser {
    private final Name name = new Name();
    private final Whitespace whitespace = new Whitespace();
    private final StartTag startTag = new StartTag();
    private final EndTag endTag = new EndTag();
    private final TextProduction textProduction = new TextProduction();

    @Override
    protected void doParse(Reader input, String fileName, BuildableDocument document) throws Exception {
        HtmlDocument documentProduction = new HtmlDocument(document, fileName);
        documentProduction.match(new Buffer(input));
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

    private class HtmlDocument implements Production<CharStream> {
        private final BuildableDocument document;
        private final String fileName;

        private HtmlDocument(BuildableDocument document, String fileName) {
            this.document = document;
            this.fileName = fileName;
        }

        public void match(CharStream stream) {
            stream.consume(whitespace);
            if (stream.consume(new HtmlElement())) {
            }
            else {
                stream.consume(new HtmlBody());
            }
            stream.consume(whitespace);
            // TODO - handle extra stuff here
        }
    }

    private class HtmlBody implements Production<CharStream> {
        public void match(CharStream stream) {
        }
    }

    private abstract class ElementProduction implements ValueProducingProduction<CharStream, Boolean> {
        public Boolean match(CharStream stream) {
            Token token = stream.consume(startTag);
            if (token == null) {
                return false;
            }
            if (!token.value.equals(getTagName())) {
                return false;
            }
//            stream.accept();
            stream.consume(getBody());
            stream.consume(endTag);
            // TODO - handle mismatched end tags here
            return true;
        }

        protected abstract Production<? super CharStream> getBody();

        protected abstract String getTagName();
    }

    private class HtmlElement extends ElementProduction {
        @Override
        protected String getTagName() {
            return "html";
        }

        @Override
        protected Production<? super CharStream> getBody() {
            return new HtmlBody();
        }
    }

    private class ParagraphElement extends ElementProduction {
        @Override
        protected String getTagName() {
            return "p";
        }

        @Override
        protected Production<? super CharStream> getBody() {
            throw new UnsupportedOperationException();
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

    private static class Whitespace implements Production<CharStream> {
        public void match(CharStream charStream) {
            // TODO - legal whitespace characters here
            while (charStream.consume(' ', '\t')) {
            }
        }
    }

    private static class Text implements Production<CharStream> {
        public void match(CharStream charStream) {
            if (!charStream.consumeAnyExcept()) {
                return;
            }
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
