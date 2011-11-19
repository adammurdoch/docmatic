package net.rubygrapefruit.docs.docbook;

import net.rubygrapefruit.docs.model.BuildableDocument;
import net.rubygrapefruit.docs.model.Document;
import net.rubygrapefruit.docs.parser.Parser;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.Reader;

/**
 * Builds a document for some Docbook input.
 */
public class DocbookParser extends Parser {
    @Override
    public Document parse(Reader input) {
        BuildableDocument document = new BuildableDocument();
        try {
            XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(input);
            try {
                StringBuilder paraContents = new StringBuilder();
                while (reader.hasNext()) {
                    XMLEvent event = reader.nextEvent();
                    if (event.isStartElement()) {
                        String elementName = event.asStartElement().getName().getLocalPart();
                        if (elementName.equals("para")) {
                            paraContents.setLength(0);
                        }
                    }
                    else if (event.isEndElement()) {
                        String elementName = event.asEndElement().getName().getLocalPart();
                        if (elementName.equals("para")) {
                            document.addParagraph().setText(paraContents.toString());
                            paraContents.setLength(0);
                        }
                    } else if (event.isCharacters()) {
                        paraContents.append(event.asCharacters().getData());
                    }
                }
            } finally {
                reader.close();
            }
        } catch (XMLStreamException e) {
            throw new RuntimeException(String.format("Could not parse docbook document.", e));
        }

        return document;
    }
}
