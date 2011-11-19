package net.rubygrapefruit.docs.parser;

import net.rubygrapefruit.docs.model.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public abstract class Parser {
    private static final Logger LOGGER = LoggerFactory.getLogger(Parser.class);
    
    public Document parse(File input) {
        LOGGER.info("Parsing {}.", input);
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
