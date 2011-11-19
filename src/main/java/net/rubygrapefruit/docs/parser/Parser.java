package net.rubygrapefruit.docs.parser;

import net.rubygrapefruit.docs.model.Document;

import java.io.*;

public abstract class Parser {
    public Document parse(File input) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(input));
            try {
                return parse(reader);
            } finally {
                reader.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(String.format("Could not parse '%s'.", input), e);
        }
    }

    public Document parse(String text) {
        return parse(new StringReader(text));
    }

    public abstract Document parse(Reader input);
}
