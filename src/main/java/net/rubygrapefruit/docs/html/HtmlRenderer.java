package net.rubygrapefruit.docs.html;

import net.rubygrapefruit.docs.model.*;
import net.rubygrapefruit.docs.model.Error;
import net.rubygrapefruit.docs.renderer.Chunk;
import net.rubygrapefruit.docs.renderer.MultiPageRenderer;
import net.rubygrapefruit.docs.renderer.Page;
import net.rubygrapefruit.docs.renderer.TitleBlock;
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
            writeChunk(page, page.getChunk(), writer);
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

    private void writeChunk(Page page, Chunk chunk, XMLStreamWriter writer) throws XMLStreamException {
        for (Block block : chunk.getContents()) {
            if (block instanceof TitleBlock) {
                TitleBlock titleBlock = (TitleBlock) block;
                writeTitle(page, titleBlock.getComponent(), 1, writer);
            } else if (block instanceof Component) {
                writeComponent(page, (Component) block, 1, writer);
            } else if (block instanceof Error) {
                writeErrorBlock((Error) block, writer);
            } else {
                writeComponentChildBlock(page, block, writer);
            }
        }
    }

    private void writeComponent(Page page, Component component, int depth, XMLStreamWriter writer) throws XMLStreamException {
        writeTitle(page, component, depth, writer);
        for (Block block : component.getContents()) {
            if (block instanceof Section) {
                Section child = (Section) block;
                writeSection(page, child, depth + 1, writer);
            } else if (block instanceof Component) {
                Component child = (Component) block;
                writeComponent(page, child, depth + 1, writer);
            } else {
                writeComponentChildBlock(page, block, writer);
            }
        }
    }

    private void writeSection(Page page, Section section, int depth, XMLStreamWriter writer) throws XMLStreamException {
        writeTitle(page, section, depth, writer);
        for (Block block : section.getContents()) {
            if (block instanceof Section) {
                Section child = (Section) block;
                writeSection(page, child, depth + 1, writer);
            } else {
                writeComponentChildBlock(page, block, writer);
            }
        }
    }

    private void writeTitle(Page page, Component component, int depth, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement("a");
        writer.writeAttribute("name", component.getId());
        writer.writeEndElement();

        if (component.getTitle().isEmpty()) {
            return;
        }

        writer.writeStartElement("h" + Math.min(depth, 6));
        writeInline(page, component.getTitle(), writer);
        writer.writeEndElement();
        writer.writeCharacters(EOL);
    }

    private void writeComponentChildBlock(Page page, Block block, XMLStreamWriter writer) throws XMLStreamException {
        if (block instanceof Paragraph) {
            Paragraph paragraph = (Paragraph) block;
            writer.writeStartElement("p");
            writeInline(page, paragraph, writer);
            writer.writeEndElement();
            writer.writeCharacters(EOL);
        } else if (block instanceof ItemisedList) {
            ItemisedList list = (ItemisedList) block;
            writer.writeStartElement("ul");
            writer.writeCharacters(EOL);
            writeItems(page, list, writer);
            writer.writeEndElement();
            writer.writeCharacters(EOL);
        } else if (block instanceof OrderedList) {
            OrderedList list = (OrderedList) block;
            writer.writeStartElement("ol");
            writer.writeCharacters(EOL);
            writeItems(page, list, writer);
            writer.writeEndElement();
            writer.writeCharacters(EOL);
        } else if (block instanceof Error) {
            writeErrorBlock((Error) block, writer);
        } else {
            throw new IllegalStateException(String.format("Don't know how to render block of type '%s'.",
                    block.getClass().getSimpleName()));
        }
    }

    private void writeErrorBlock(Error error, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement("div");
        writer.writeAttribute("class", "unknown");
        writer.writeCharacters(error.getMessage());
        writer.writeEndElement();
        writer.writeCharacters(EOL);
    }

    private void writeInline(Page page, InlineContainer inline, XMLStreamWriter writer) throws XMLStreamException {
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
                writeCrossReference(page, (CrossReference) element, writer);
            } else if (element instanceof Error) {
                writeErrorInline((Error) element, writer);
            } else {
                throw new IllegalStateException(String.format("Don't know how to render inline of type '%s'.",
                        element.getClass().getSimpleName()));
            }
        }
    }

    private void writeCrossReference(Page page, CrossReference crossReference, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement("a");
        Referenceable target = crossReference.getTarget();
        Page targetPage = page.getPageFor(target);
        if (targetPage != page) {
            writer.writeAttribute("href", page.getUrlTo(targetPage) + "#" + target.getId());
        } else {
            writer.writeAttribute("href", "#" + target.getId());
        }
        writer.writeCharacters(target.getReferenceText());
        writer.writeEndElement();
    }

    private void writeErrorInline(Error error, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement("span");
        writer.writeAttribute("class", "unknown");
        writer.writeCharacters(error.getMessage());
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

    private void writeItems(Page page, List list, XMLStreamWriter writer) throws XMLStreamException {
        for (ListItem item : list.getItems()) {
            writer.writeStartElement("li");
            writer.writeCharacters(EOL);
            for (Block childBlock : item.getContents()) {
                writeComponentChildBlock(page, childBlock, writer);
            }
            writer.writeEndElement();
            writer.writeCharacters(EOL);
        }
    }
}
