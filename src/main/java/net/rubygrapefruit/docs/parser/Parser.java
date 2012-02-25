package net.rubygrapefruit.docs.parser;

import net.rubygrapefruit.docs.model.BuildableDocument;
import net.rubygrapefruit.docs.model.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public abstract class Parser {
    private static final Logger LOGGER = LoggerFactory.getLogger(Parser.class);
    
    public Document parse(File input) throws ParseException {
        LOGGER.info("Parsing {}.", input);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(input));
            try {
                return doParse(reader, input.getPath());
            } finally {
                reader.close();
            }
        } catch (Exception e) {
            throw new ParseException(String.format("Could not parse '%s'.", input), e);
        }
    }

    public Document parse(String text, String fileName) throws ParseException {
        LOGGER.info("Parsing {}.", fileName);
        try {
            return doParse(new StringReader(text), fileName);
        } catch (Exception e) {
            throw new ParseException(String.format("Could not parse '%s'.", fileName), e);
        }
    }

    public Document parse(Reader input, String fileName) throws ParseException {
        LOGGER.info("Parsing {}.", fileName);
        try {
            return doParse(input, fileName);
        } catch (Exception e) {
            throw new ParseException(String.format("Could not parse '%s'.", fileName), e);
        }
    }

    private Document doParse(Reader input, String fileName) throws Exception {
        BuildableDocument document = new BuildableDocument();
        doParse(input, fileName, document);
        document.finish();
        return document;
    }

    protected abstract void doParse(Reader input, String fileName, BuildableDocument document) throws Exception;
}
