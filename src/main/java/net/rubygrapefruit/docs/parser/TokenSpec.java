package net.rubygrapefruit.docs.parser;

import java.io.IOException;

public interface TokenSpec {
    void match(Buffer buffer) throws IOException;
}
