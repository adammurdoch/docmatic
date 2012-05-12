package net.rubygrapefruit.docs.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TokenStream<S extends MarkableStream, T> {
    private final S stream;

    public TokenStream(S stream) {
        this.stream = stream;
    }

    public <T> T consume(ValueProducingProduction<? super S, T> production) {
        stream.start();
        T value = production.match(stream);
        if (value != null) {
            stream.commit();
        } else {
            stream.rollback();
        }
        return value;
    }
}
