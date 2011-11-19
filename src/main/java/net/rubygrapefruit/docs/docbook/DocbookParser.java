package net.rubygrapefruit.docs.docbook;

import net.rubygrapefruit.docs.model.BuildableDocument;
import net.rubygrapefruit.docs.model.BuildableParagraph;
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
                BuildableParagraph currentPara = null;
                while (reader.hasNext()) {
                    XMLEvent event = reader.nextEvent();
                    if (event.isStartElement()) {
                        String elementName = event.asStartElement().getName().getLocalPart();
                        if (elementName.equals("para")) {
                            currentPara = document.addParagraph();
                        }
                    }
                    else if (event.isEndElement()) {
                        String elementName = event.asEndElement().getName().getLocalPart();
                        if (elementName.equals("para")) {
                            currentPara = null;
                        }
                    } else if (event.isCharacters() && currentPara != null) {
                        currentPara.append(event.asCharacters().getData());
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
