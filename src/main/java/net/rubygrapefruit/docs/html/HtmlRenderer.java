package net.rubygrapefruit.docs.html;

import net.rubygrapefruit.docs.model.*;
import net.rubygrapefruit.docs.renderer.*;
import net.rubygrapefruit.docs.theme.TextTheme;
import net.rubygrapefruit.docs.theme.Theme;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.OutputStream;

public class HtmlRenderer extends MultiPageRenderer {
    private final String EOL = String.format("%n");

    @Override
    protected void doRender(Page page, Theme theme, OutputStream stream) throws Exception {
        XMLStreamWriter writer = XMLOutputFactory.newFactory().createXMLStreamWriter(stream, "utf-8");
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
            TextTheme textTheme = theme.getAspect(TextTheme.class);
            if (textTheme != null) {
                writer.writeCharacters("html { font-family: ");
                writer.writeCharacters(textTheme.getFontName());
                writer.writeCharacters("; font-size: 12pt; color: #");
                writer.writeCharacters(String.format("%02x%02x%02x", textTheme.getColour().getRed(),
                        textTheme.getColour().getGreen(), textTheme.getColour().getBlue()));
                writer.writeCharacters("; line-height: normal;");
                writer.writeCharacters("}\n");
                writer.writeCharacters("body { margin: 3em 5em; background-color: white; }\n");
                writer.writeCharacters("p { line-height: ");
                writer.writeCharacters(textTheme.getLineSpacing().toString());
                writer.writeCharacters("; }\n");
                writer.writeCharacters("div.header { overflow: auto; height: 2em; margin-bottom: 1.5em; }\n");
                writer.writeCharacters("div.footer { overflow: auto; margin-top: 6.5em; }\n");
                writer.writeCharacters("div.navbar { text-align: right; }\n");
                writer.writeCharacters("div.navbar a { color: #909090; margin-left: 2em; }\n");
                writer.writeCharacters("div.navbar a:visited { color: #909090; }\n");
            }
            writer.writeCharacters(".unknown { color: red; }\n");
            HtmlTheme htmlTheme = theme.getAspect(HtmlTheme.class);
            if (htmlTheme != null) {
                StringBuilder rules = new StringBuilder();
                htmlTheme.writeStyleRules(rules);
                writer.writeCharacters(rules.toString());
            }
            writer.writeEndElement();
            writer.writeCharacters(EOL);
            writer.writeEndElement();
            writer.writeCharacters(EOL);
            writer.writeStartElement("body");
            writer.writeCharacters(EOL);
            writeHeader(page, writer);
            writeChunk(page.getChunk(), writer);
            writeFooter(page, writer);
            writer.writeEndElement();
            writer.writeCharacters(EOL);
            writer.writeEndElement();
            writer.writeCharacters(EOL);
        } finally {
            writer.close();
        }
    }

    private void writeHeader(Page page, XMLStreamWriter writer) throws XMLStreamException {
        writeNavLinks(page, "header", writer);
    }

    private void writeFooter(Page page, XMLStreamWriter writer) throws XMLStreamException {
        writeNavLinks(page, "footer", writer);
    }

    private void writeNavLinks(Page page, String htmlClass, XMLStreamWriter writer) throws XMLStreamException {
        if (page.getPreviousUrl() == null && page.getHomeUrl() == null && page.getNextUrl() == null) {
            return;
        }
        writer.writeStartElement("div");
        writer.writeAttribute("class", "navbar " + htmlClass);
        if (page.getPreviousUrl() != null) {
            writer.writeStartElement("a");
            writer.writeAttribute("href", page.getPreviousUrl());
            writer.writeAttribute("class", "previouslink");
            writer.writeCharacters("Previous");
            writer.writeEndElement();
        }
        if (page.getHomeUrl() != null) {
            writer.writeStartElement("a");
            writer.writeAttribute("href", page.getHomeUrl());
            writer.writeAttribute("class", "homelink");
            writer.writeCharacters("Home");
            writer.writeEndElement();
        }
        if (page.getNextUrl() != null) {
            writer.writeStartElement("a");
            writer.writeAttribute("href", page.getNextUrl());
            writer.writeAttribute("class", "nextlink");
            writer.writeCharacters("Next");
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    private void writeChunk(Chunk chunk, XMLStreamWriter writer) throws XMLStreamException {
        for (Block block : chunk.getContents()) {
            if (block instanceof TitleBlock) {
                TitleBlock titleBlock = (TitleBlock) block;
                writeTitle(titleBlock.getComponent(), 1, writer);
            } else if (block instanceof Component) {
                writeComponent((Component) block, 1, writer);
            } else if (block instanceof Unknown) {
                writeUnknownBlock((Unknown) block, writer);
            } else {
                writeComponentChildBlock(block, writer);
            }
        }
    }

    private void writeComponent(Component component, int depth, XMLStreamWriter writer) throws XMLStreamException {
        writeTitle(component, depth, writer);
        for (Block block : component.getContents()) {
            if (block instanceof Section) {
                Section child = (Section) block;
                writeSection(child, depth + 1, writer);
            } else if (block instanceof Component) {
                Component child = (Component) block;
                writeComponent(child, depth + 1, writer);
            } else {
                writeComponentChildBlock(block, writer);
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
                writeComponentChildBlock(block, writer);
            }
        }
    }

    private void writeTitle(Component component, int depth, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement("a");
        writer.writeAttribute("name", component.getId());
        writer.writeEndElement();

        if (component.getTitle().isEmpty()) {
            return;
        }

        writer.writeStartElement("h" + Math.min(depth, 6));
        writeInline(component.getTitle(), writer);
        writer.writeEndElement();
        writer.writeCharacters(EOL);
    }

    private void writeComponentChildBlock(Block block, XMLStreamWriter writer) throws XMLStreamException {
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
            writeUnknownBlock((Unknown) block, writer);
        } else {
            throw new IllegalStateException(String.format("Don't know how to render block of type '%s'.",
                    block.getClass().getSimpleName()));
        }
    }

    private void writeUnknownBlock(Unknown unknown, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement("div");
        writer.writeAttribute("class", "unknown");
        writer.writeCharacters(unknown.getMessage());
        writer.writeEndElement();
        writer.writeCharacters(EOL);
    }

    private void writeInline(InlineContainer inline, XMLStreamWriter writer) throws XMLStreamException {
        for (Inline element : inline.getContents()) {
            if (element instanceof Text) {
                Text text = (Text) element;
                writer.writeCharacters(text.getText());
            } else if (element instanceof Code) {
                writeCodeInline(element, "code", writer);
            } else if (element instanceof Literal) {
                writeCodeInline(element, "literal", writer);
            } else if (element instanceof Emphasis) {
                writeEmphasisInline(element, writer);
            } else if (element instanceof CrossReference) {
                writeCrossReference((CrossReference) element, writer);
            } else if (element instanceof Unknown) {
                writerUnknownInline((Unknown) element, writer);
            } else {
                throw new IllegalStateException(String.format("Don't know how to render inline of type '%s'.",
                        element.getClass().getSimpleName()));
            }
        }
    }

    private void writeCrossReference(CrossReference crossReference, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement("a");
        InternalTarget target = (InternalTarget) crossReference.getTarget();
        writer.writeAttribute("href", "#" + target.getElement().getId());
        writer.writeCharacters(target.getElement().getReferenceText());
        writer.writeEndElement();
    }

    private void writerUnknownInline(Unknown unknown, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement("span");
        writer.writeAttribute("class", "unknown");
        writer.writeCharacters(unknown.getMessage());
        writer.writeEndElement();
        writer.writeCharacters(EOL);
    }

    private void writeCodeInline(Inline code, String htmlClass, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement("code");
        writer.writeAttribute("class", htmlClass);
        writer.writeCharacters(code.getText());
        writer.writeEndElement();
    }

    private void writeEmphasisInline(Inline code, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement("em");
        writer.writeCharacters(code.getText());
        writer.writeEndElement();
    }

    private void writeItems(List list, XMLStreamWriter writer) throws XMLStreamException {
        for (ListItem item : list.getItems()) {
            writer.writeStartElement("li");
            writer.writeCharacters(EOL);
            for (Block childBlock : item.getContents()) {
                writeComponentChildBlock(childBlock, writer);
            }
            writer.writeEndElement();
            writer.writeCharacters(EOL);
        }
    }
}
