package net.rubygrapefruit.docs.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TokenStream<S extends MarkableStream, T> {
    private final BufferingConsumer<T> buffer = new BufferingConsumer<T>();
    private final S stream;

    public TokenStream(S stream) {
        this.stream = stream;
    }

    public boolean consume(TokenGeneratingProduction<? super S, T> production, Collection<T> consumer) {
        try {
            stream.start();
            boolean matched = production.match(stream, buffer);
            if (matched) {
                stream.commit();
                for (T token : buffer.tokens) {
                    consumer.add(token);
                }
            } else {
                stream.rollback();
            }
            return matched;
        } finally {
            buffer.tokens.clear();
        }
    }

    private static class BufferingConsumer<T> implements TokenConsumer<T> {
        private final List<T> tokens = new ArrayList<T>();

        public void consume(T token) {
            tokens.add(token);
        }
    }
}
