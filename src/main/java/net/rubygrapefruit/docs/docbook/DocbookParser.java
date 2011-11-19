package net.rubygrapefruit.docs.docbook;

import net.rubygrapefruit.docs.model.*;
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
        DocumentBuilder builder = new DocumentBuilder(document);
        BuildableParagraph currentPara = null;
        StringBuilder currentText = null;
        try {
            XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(input);
            try {
                while (reader.hasNext()) {
                    XMLEvent event = reader.nextEvent();
                    if (event.isStartElement()) {
                        String elementName = event.asStartElement().getName().getLocalPart();
                        if (elementName.equals("chapter") || elementName.equals("section")) {
                            builder.appendSection();
                        }
                        else if (elementName.equals("para")) {
                            currentPara = builder.appendParagraph();
                        }
                        else if (elementName.equals("title")) {
                            currentText = new StringBuilder();
                        }
                    }
                    else if (event.isEndElement()) {
                        String elementName = event.asEndElement().getName().getLocalPart();
                        if (elementName.equals("chapter") || elementName.equals("section")) {
                            builder.popSection();
                        }
                        else if (elementName.equals("para")) {
                            currentPara = null;
                        }
                        else if (elementName.equals("title")) {
                            builder.getCurrentSection().setTitle(currentText);
                            currentText = null;
                        }
                    } else if (event.isCharacters() && currentText != null) {
                        currentText.append(event.asCharacters().getData());
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
