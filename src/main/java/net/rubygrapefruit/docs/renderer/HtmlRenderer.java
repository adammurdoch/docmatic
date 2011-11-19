package net.rubygrapefruit.docs.renderer;

import net.rubygrapefruit.docs.model.*;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.OutputStream;

public class HtmlRenderer extends Renderer {
    private final String EOL = String.format("%n");

    @Override
    protected void doRender(Document document, OutputStream outputStream) throws Exception {
        XMLStreamWriter writer = XMLOutputFactory.newFactory().createXMLStreamWriter(outputStream);
        try {
            writer.writeDTD(
                    "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">");
            writer.writeCharacters(EOL);
            writer.writeStartElement("html");
            writer.writeCharacters(EOL);
            writer.writeStartElement("head");
            writer.writeCharacters(EOL);
            writer.writeStartElement("style");
            writer.writeCharacters(EOL);
            writer.writeCharacters("html { font-family: sans-serif; font-size: 12pt; }");
            writer.writeCharacters("body { margin: 5em; }");
            writer.writeCharacters(".unknown { color: red; }");
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
    }

    private void writeSection(Component component, int depth, XMLStreamWriter writer) throws XMLStreamException {
        for (Block block : component.getContents()) {
            if (block instanceof Section) {
                Section child = (Section) block;
                writer.writeStartElement("h" + (depth + 1));
                writer.writeCharacters(child.getTitle());
                writer.writeEndElement();
                writer.writeCharacters(EOL);
                writeSection(child, depth + 1, writer);
            } else if (block instanceof Paragraph) {
                Paragraph paragraph = (Paragraph) block;
                writer.writeStartElement("p");
                writer.writeCharacters(paragraph.getText());
                writer.writeEndElement();
                writer.writeCharacters(EOL);
            } else if (block instanceof UnknownBlock) {
                UnknownBlock unknownBlock = (UnknownBlock) block;
                writer.writeStartElement("div");
                writer.writeAttribute("class", "unknown");
                writer.writeCharacters("Unexpected ");
                writer.writeCharacters(unknownBlock.getName());
                writer.writeCharacters(" found at ");
                writer.writeCharacters(unknownBlock.getLocation().getFile());
                writer.writeCharacters(", line: ");
                writer.writeCharacters(String.valueOf(unknownBlock.getLocation().getLine()));
                writer.writeCharacters(", column: ");
                writer.writeCharacters(String.valueOf(unknownBlock.getLocation().getColumn()));
                writer.writeEndElement();
                writer.writeCharacters(EOL);
            } else {
                throw new IllegalStateException(String.format("Don't know how to render block of type '%s'.",
                        block.getClass().getSimpleName()));
            }
        }
    }
}
