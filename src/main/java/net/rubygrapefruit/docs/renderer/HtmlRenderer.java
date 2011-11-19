package net.rubygrapefruit.docs.renderer;

import net.rubygrapefruit.docs.model.Block;
import net.rubygrapefruit.docs.model.Document;
import net.rubygrapefruit.docs.model.Paragraph;
import net.rubygrapefruit.docs.model.Section;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.*;

public class HtmlRenderer extends Renderer {
    private final String EOL = String.format("%n");

    public void render(Document document, OutputStream outputStream) {
        try {
            XMLStreamWriter writer = XMLOutputFactory.newFactory().createXMLStreamWriter(outputStream);
            try {
                writer.writeDTD("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">");
                writer.writeCharacters(EOL);
                writer.writeStartElement("html");
                writer.writeCharacters(EOL);
                writer.writeStartElement("head");
                writer.writeCharacters(EOL);
                writer.writeStartElement("style");
                writer.writeCharacters(EOL);
                writer.writeCharacters("html { font-family: sans-serif; font-size: 12pt; }");
                writer.writeCharacters("body { margin: 5em; }");
                writer.writeEndElement();
                writer.writeCharacters(EOL);
                writer.writeEndElement();
                writer.writeCharacters(EOL);
                writer.writeStartElement("body");
                writer.writeCharacters(EOL);
                writeSection(document, 0, writer);
                writer.writeEndElement();
                writer.writeCharacters(EOL);
                writer.writeEndElement();
                writer.writeCharacters(EOL);
            } finally {
                writer.close();
            }
        } catch (XMLStreamException e) {
            throw new RuntimeException("Could not write document.", e);
        }
    }

    private void writeSection(Section section, int depth, XMLStreamWriter writer) throws XMLStreamException {
        for (Block block : section.getContents()) {
            if (block instanceof Section) {
                Section child = (Section) block;
                writer.writeStartElement("h" + (depth+1));
                writer.writeCharacters(child.getTitle());
                writer.writeEndElement();
                writer.writeCharacters(EOL);
                writeSection(child, depth + 1, writer);
            } else {
                Paragraph paragraph = (Paragraph) block;
                writer.writeStartElement("p");
                writer.writeCharacters(paragraph.getText());
                writer.writeEndElement();
                writer.writeCharacters(EOL);
            }
        }
    }
}
