package net.rubygrapefruit.docs.parser;

public class Productions {
    /**
     * Matches the given character.
     */
    public static Production<CharStream> match(final char candidate) {
        return new Production<CharStream>() {
            public void match(CharStream stream) {
                stream.consume(candidate);
            }
        };
    }

    /**
     * Matches the given string.
     */
    public static Production<CharStream> match(final String chars) {
        return new Production<CharStream>() {
            public void match(CharStream stream) {
                for (int i = 0; i < chars.length(); i++) {
                    if (!stream.consume(chars.charAt(i))) {
                        stream.rewind();
                        return;
                    }
                }
            }
        };
    }

    /**
     * Matches a character form the given range.
     */
    public static Production<CharStream> matchFromRange(final char from, final char to) {
        return new Production<CharStream>() {
            public void match(CharStream stream) {
                stream.consumeRange(from, to);
            }
        };
    }

    /**
     * Matches one or more of the given characters.
     */
    public static Production<CharStream> matchAtLeastOneOf(final char... candidates) {
        return new Production<CharStream>() {
            public void match(CharStream stream) {
                while (stream.consume(candidates)) {
                }
            }
        };
    }

    /**
     * Matches one or more of the given production.
     */
    public static Production<CharStream> matchAtLeastOnce(final Production<? super CharStream> production) {
        return new Production<CharStream>() {
            public void match(CharStream charStream) {
                while (charStream.consume(production)) {
                }
            }
        };
    }

    /**
     * Matches the first of the given candidates.
     */
    public static Production<CharStream> matchFirstOf(final Production<? super CharStream>... candidates) {
        return new Production<CharStream>() {
            public void match(CharStream stream) {
                for (int i = 0; i < candidates.length; i++) {
                    Production<? super CharStream> candidate = candidates[i];
                    if (stream.consume(candidate)) {
                        return;
                    }
                }
            }
        };
    }
}
