package net.rubygrapefruit.docs.html;

import net.rubygrapefruit.docs.model.*;
import net.rubygrapefruit.docs.renderer.Renderer;
import net.rubygrapefruit.docs.renderer.TextTheme;
import net.rubygrapefruit.docs.renderer.Theme;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.OutputStream;

public class HtmlRenderer extends Renderer {
    private final String EOL = String.format("%n");

    @Override
    protected void doRender(Document document, Theme theme, OutputStream outputStream) throws Exception {
        XMLStreamWriter writer = XMLOutputFactory.newFactory().createXMLStreamWriter(outputStream);
        try {
            writer.writeDTD(
                    "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">");
            writer.writeCharacters(EOL);
            writer.writeStartElement("html");
            writer.writeCharacters(EOL);
            writer.writeStartElement("head");
            writer.writeCharacters(EOL);
            writer.writeStartElement("meta");
            writer.writeAttribute("http-equiv", "Content-Type");
            writer.writeAttribute("content", "text/html; charset=UTF-8");
            writer.writeEndElement();
            writer.writeCharacters(EOL);
            writer.writeStartElement("style");
            writer.writeCharacters(EOL);
            TextTheme textTheme = theme.asTextTheme();
            if (textTheme != null) {
                writer.writeCharacters("html { font-family: ");
                writer.writeCharacters(textTheme.getFontName());
                writer.writeCharacters("; font-size: 12pt; color: #");
                writer.writeCharacters(String.format("%02x%02x%02x", textTheme.getColour().getRed(), textTheme.getColour().getGreen(), textTheme.getColour().getBlue()));
                writer.writeCharacters("; line-height: normal;");
                writer.writeCharacters("}\n");
                writer.writeCharacters("p { line-height: ");
                writer.writeCharacters(textTheme.getLineSpacing().toString());
                writer.writeCharacters("; }\n");
                writer.writeCharacters("body { margin: 5em; }\n");
            }
            writer.writeCharacters(".unknown { color: red; }\n");
            writer.writeEndElement();
            writer.writeCharacters(EOL);
            writer.writeEndElement();
            writer.writeCharacters(EOL);
            writer.writeStartElement("body");
            writer.writeCharacters(EOL);
            writeComponent(document, 1, writer);
            writer.writeEndElement();
            writer.writeCharacters(EOL);
            writer.writeEndElement();
            writer.writeCharacters(EOL);
        } finally {
            writer.close();
        }
    }

    private void writeComponent(Component component, int depth, XMLStreamWriter writer) throws XMLStreamException {
        writeTitle(component, 1, writer);
        for (Block block : component.getContents()) {
            if (block instanceof Section) {
                Section child = (Section) block;
                writeSection(child, depth == 1 ? 1: 2, writer);
            } else if (block instanceof Component) {
                Component child = (Component) block;
                writeComponent(child, depth + 1, writer);
            } else {
                writeBlock(block, writer);
            }
        }
    }

    private void writeSection(Section section, int depth, XMLStreamWriter writer) throws XMLStreamException {
        writeTitle(section, depth, writer);
        for (Block block : section.getContents()) {
            if (block instanceof Section) {
                Section child = (Section) block;
                writeSection(child, depth + 1, writer);
            } else {
                writeBlock(block, writer);
            }
        }
    }

    private void writeTitle(Component component, int depth, XMLStreamWriter writer) throws XMLStreamException {
        if (component.getTitle().isEmpty()) {
            return;
        }
        writer.writeStartElement("h" + Math.min(depth, 6));
        writeInline(component.getTitle(), writer);
        writer.writeEndElement();
        writer.writeCharacters(EOL);
    }

    private void writeBlock(Block block, XMLStreamWriter writer) throws XMLStreamException {
        if (block instanceof Paragraph) {
            Paragraph paragraph = (Paragraph) block;
            writer.writeStartElement("p");
            writeInline(paragraph, writer);
            writer.writeEndElement();
            writer.writeCharacters(EOL);
        } else if (block instanceof ItemisedList) {
            ItemisedList list = (ItemisedList) block;
            writer.writeStartElement("ul");
            writer.writeCharacters(EOL);
            writeItems(list, writer);
            writer.writeEndElement();
            writer.writeCharacters(EOL);
        } else if (block instanceof OrderedList) {
            OrderedList list = (OrderedList) block;
            writer.writeStartElement("ol");
            writer.writeCharacters(EOL);
            writeItems(list, writer);
            writer.writeEndElement();
            writer.writeCharacters(EOL);
        } else if (block instanceof Unknown) {
            Unknown unknown = (Unknown) block;
            writer.writeStartElement("div");
            writer.writeAttribute("class", "unknown");
            writer.writeCharacters(unknown.getMessage());
            writer.writeEndElement();
            writer.writeCharacters(EOL);
        } else {
            throw new IllegalStateException(String.format("Don't know how to render block of type '%s'.",
                    block.getClass().getSimpleName()));
        }
    }

    private void writeInline(InlineContainer inline, XMLStreamWriter writer) throws XMLStreamException {
        for (Inline element : inline.getContents()) {
            if (element instanceof Text) {
                Text text = (Text) element;
                writer.writeCharacters(text.getText());
            } else if (element instanceof Unknown) {
                Unknown unknown = (Unknown) element;
                writer.writeStartElement("span");
                writer.writeAttribute("class", "unknown");
                writer.writeCharacters(unknown.getMessage());
                writer.writeEndElement();
                writer.writeCharacters(EOL);
            } else {
                throw new IllegalStateException(String.format("Don't know how to render inline of type '%s'.",
                        element.getClass().getSimpleName()));
            }
        }
    }

    private void writeItems(List list, XMLStreamWriter writer) throws XMLStreamException {
        for (ListItem item : list.getItems()) {
            writer.writeStartElement("li");
            writer.writeCharacters(EOL);
            for (Block childBlock : item.getContents()) {
                writeBlock(childBlock, writer);
            }
            writer.writeEndElement();
            writer.writeCharacters(EOL);
        }
    }
}
