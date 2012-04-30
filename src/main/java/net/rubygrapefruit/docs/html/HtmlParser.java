package net.rubygrapefruit.docs.html;

import net.rubygrapefruit.docs.model.buildable.BuildableDocument;
import net.rubygrapefruit.docs.model.buildable.BuildableParagraph;
import net.rubygrapefruit.docs.parser.*;

import java.io.Reader;
import java.util.Collection;

/**
 * doc = element element = empty-element | element-with-content empty-element = '<' element-name '/>'
 * element-with-content = start-element (element | text | cdata)* end-element start-element = '<' element-name '>'
 * end-element = '</' element-name '>' text = (!reserved-char | entity)+ cdata = ??
 */
public class HtmlParser extends Parser {
    @Override
    protected void doParse(Reader input, String fileName, BuildableDocument document) throws Exception {
        Buffer buffer = new Buffer(input);
        ElementStream elementStream = new ElementStream(buffer);
        BuildableParagraph paragraph = null;
        while (elementStream.peek() != null) {
            Token token = elementStream.next();
            switch (token.type) {
                case StartElement:
                    if (token.value.equals("p")) {
                        paragraph = document.addParagraph();
                    } else {
                        document.addError(String.format("<%s>unknown element</%s>", token.value, token.value));
                    }
                    break;
                case EndElement:
                    if (token.value.equals("p")) {
                        paragraph = null;
                    }
                    break;
                case Text:
                    if (paragraph == null) {
                        paragraph = document.addParagraph();
                    }
                    paragraph.append(token.value);
                    break;
            }
        }
    }

    private enum TokenType {StartElement, Text, EndElement}

    private static class Token {
        private final TokenType type;
        private final String value;

        private Token(TokenType type, String value) {
            this.type = type;
            this.value = value;
        }
    }

    private static class ElementStream extends LookaheadStream<Token> {
        private final TokenStream<Buffer, Token> stream;
        private final Name name = new Name();
        private final StartTag startTag = new StartTag(name);
        private final EndTag endTag = new EndTag(name);
        private final TextProduction text = new TextProduction();

        private ElementStream(Buffer buffer) {
            this.stream = new TokenStream<Buffer, Token>(buffer);
        }

        @Override
        protected void readNext(Collection<Token> elements) {
            if (stream.consume(startTag, elements)) {
                ;
            } else if (stream.consume(endTag, elements)) {
                ;
            } else if (stream.consume(text, elements)) {
                ;
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

    private static class StartTag implements TokenGeneratingProduction<CharStream, Token> {
        private final Name name;

        private StartTag(Name name) {
            this.name = name;
        }

        public boolean match(CharStream charStream, TokenConsumer<Token> dest) {
            if (!charStream.consume('<')) {
                return false;
            }
            // TODO - whitespace here?
            if (!charStream.consume(name)) {
                return false;
            }
            String name = charStream.getValue();
            // TODO - need real whitespace here
            while (charStream.consume(' ', '\t')) {
            }
            // TODO - attributes here
            if (!charStream.consume('>')) {
                return false;
            }
            dest.consume(new Token(TokenType.StartElement, name));
            return true;
        }
    }

    private static class EndTag implements TokenGeneratingProduction<CharStream, Token> {
        private final Name name;

        private EndTag(Name name) {
            this.name = name;
        }

        public boolean match(CharStream charStream, TokenConsumer<Token> dest) {
            if (!charStream.consume('<')) {
                return false;
            }
            if (!charStream.consume('/')) {
                return false;
            }
            // TODO - whitespace here?
            if (!charStream.consume(name)) {
                return false;
            }
            String name = charStream.getValue();
            // TODO - need real whitespace here
            while (charStream.consume(' ', '\t')) {
            }
            if (!charStream.consume('>')) {
                return false;
            }
            dest.consume(new Token(TokenType.EndElement, name));
            return true;
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

    private static class TextProduction implements TokenGeneratingProduction<CharStream, Token> {
        final Text text = new Text();
        public boolean match(CharStream charStream, TokenConsumer<Token> dest) {
            if (!charStream.consume(text)) {
                return false;
            }
            dest.consume(new Token(TokenType.Text, charStream.getValue()));
            return true;
        }
    }
}
