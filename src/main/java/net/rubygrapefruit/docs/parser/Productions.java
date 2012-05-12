package net.rubygrapefruit.docs.parser;

public class Productions {
    public static Production<CharStream> match(char candidate) {
        return new SingleCharProduction(candidate);
    }

    public static Production<CharStream> matchFromRange(char from, char to) {
        return new CharRangeProduction(from, to);
    }

    public static Production<CharStream> matchOneOf(char... candidates) {
        return new SingleCharProduction(candidates);
    }

    public static Production<CharStream> matchAtLeastOneOf(char... candidates) {
        return new CharSequenceProduction(candidates);
    }

    public static Production<CharStream> matchAtLeastOnce(final Production<? super CharStream> production) {
        return new Production<CharStream>() {
            public void match(CharStream charStream) {
                if (!charStream.consume(production)) {
                    return;
                }
                while (charStream.consume(production)) {
                    ;
                }
            }
        };
    }

    private static class CharSequenceProduction implements Production<CharStream> {
        private final char[] candidates;

        private CharSequenceProduction(char... candidates) {
            this.candidates = candidates;
        }

        public void match(CharStream charStream) {
            while (charStream.consume(candidates)) {
            }
        }
    }

    private static class CharRangeProduction implements Production<CharStream> {
        private final char from;
        private final char to;

        private CharRangeProduction(char from, char to) {
            this.from = from;
            this.to = to;
        }

        public void match(CharStream charStream) {
            charStream.consumeRange(from, to);
        }
    }

    private static class SingleCharProduction implements Production<CharStream> {
        private final char[] candidates;

        private SingleCharProduction(char... candidates) {
            this.candidates = candidates;
        }

        public void match(CharStream charStream) {
            charStream.consume(candidates);
        }
    }
}
