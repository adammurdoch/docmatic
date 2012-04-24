package net.rubygrapefruit.docs.parser;

public class Productions {
    public static CharProduction match(char candidate) {
        return new SingleCharProduction(candidate);
    }

    public static CharProduction matchFromRange(char from, char to) {
        return new CharRangeProduction(from, to);
    }

    public static CharProduction matchOneOf(char... candidates) {
        return new SingleCharProduction(candidates);
    }

    public static CharProduction matchAtLeastOneOf(char... candidates) {
        return new CharSequenceProduction(candidates);
    }

    private static class CharSequenceProduction implements CharProduction {
        private final char[] candidates;

        private CharSequenceProduction(char... candidates) {
            this.candidates = candidates;
        }

        public void match(CharStream charStream) {
            while (charStream.consume(candidates)) {
            }
        }
    }

    private static class CharRangeProduction implements CharProduction {
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

    private static class SingleCharProduction implements CharProduction {
        private final char[] candidates;

        private SingleCharProduction(char... candidates) {
            this.candidates = candidates;
        }

        public void match(CharStream charStream) {
            charStream.consume(candidates);
        }
    }
}
