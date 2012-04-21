package net.rubygrapefruit.docs.docbook;

import net.rubygrapefruit.docs.model.Component;
import net.rubygrapefruit.docs.model.buildable.*;
import net.rubygrapefruit.docs.parser.Parser;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.Reader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Builds a document for some Docbook input.
 */
public class DocbookParser extends Parser {
    @Override
    protected void doParse(Reader input, String fileName, BuildableDocument document) throws Exception {
        final LinkedList<ElementHandler> handlerStack = new LinkedList<ElementHandler>();
        final LinkedList<String> elementNameStack = new LinkedList<String>();
        int pos = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf(File.separatorChar));
        if (pos >= 0) {
            fileName = fileName.substring(pos + 1);
        }
        final DefaultContext context = new DefaultContext(fileName);
        context.pushContainer(document);
        handlerStack.add(new RootHandler());
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        saxParserFactory.setNamespaceAware(true);
        saxParserFactory.setXIncludeAware(true);
        saxParserFactory.setValidating(false);
        SAXParser saxParser = saxParserFactory.newSAXParser();
        saxParser.getXMLReader().setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        saxParser.getXMLReader().setFeature("http://xml.org/sax/features/validation", false);
        saxParser.getXMLReader().setContentHandler(new DefaultHandler() {
            @Override
            public void setDocumentLocator(Locator locator) {
                context.location = locator;
            }

            @Override
            public void startElement(String uri, String elementName, String qName, Attributes attributes)
                    throws SAXException {
                ElementHandler childHandler = handlerStack.getLast().pushChild(elementName, context);
                elementNameStack.add(elementName);
                handlerStack.add(childHandler);
                context.setElementName(elementName);
                childHandler.start(elementName, attributes, context);
            }

            @Override
            public void characters(char[] chars, int start, int length) throws SAXException {
                handlerStack.getLast().handleText(new String(chars, start, length), context);
            }

            @Override
            public void endElement(String uri, String elementName, String qName) throws SAXException {
                handlerStack.removeLast().finish(context);
                elementNameStack.removeLast();
                context.setElementName(elementNameStack.isEmpty() ? null : elementNameStack.getLast());
            }
        });
        saxParser.getXMLReader().parse(new InputSource(input));
    }

    private interface Context {
        String getFileName();

        int getLineNumber();

        int getColumnNumber();

        BuildableComponent currentComponent();

        BuildableBlockContainer currentContainer();

        BuildableInlineContainer currentInline();

        void pushInline(BuildableInlineContainer container);

        void popInline();

        void pushContainer(BuildableBlockContainer container);

        void popContainer();

        Map<String, Component> getComponentsById();

        String getElementName();
    }

    private static class DefaultContext implements Context {
        final String fileName;
        final LinkedList<BuildableBlockContainer> containerStack = new LinkedList<BuildableBlockContainer>();
        final LinkedList<BuildableComponent> componentStack = new LinkedList<BuildableComponent>();
        final LinkedList<BuildableInlineContainer> inlineStack = new LinkedList<BuildableInlineContainer>();
        final Map<String, Component> componentsById = new HashMap<String, Component>();
        Locator location;
        private String elementName;

        private DefaultContext(String fileName) {
            this.fileName = fileName;
        }

        public String getFileName() {
            return fileName;
        }

        public int getLineNumber() {
            return location.getLineNumber();
        }

        public int getColumnNumber() {
            return location.getColumnNumber();
        }

        public Map<String, Component> getComponentsById() {
            return componentsById;
        }

        public BuildableComponent currentComponent() {
            return componentStack.getLast();
        }

        public BuildableBlockContainer currentContainer() {
            return containerStack.getLast();
        }

        public BuildableInlineContainer currentInline() {
            return inlineStack.getLast();
        }

        public void pushInline(BuildableInlineContainer container) {
            inlineStack.add(container);
        }

        public void popInline() {
            inlineStack.removeLast();
        }

        public void pushContainer(BuildableBlockContainer container) {
            containerStack.add(container);
            if (container instanceof BuildableComponent) {
                componentStack.add((BuildableComponent) container);
            }
        }

        public void popContainer() {
            assert inlineStack.isEmpty();
            BuildableBlockContainer old = containerStack.removeLast();
            if (old == componentStack.getLast()) {
                componentStack.removeLast();
            }
        }

        public String getElementName() {
            return elementName;
        }

        public void setElementName(String elementName) {
            this.elementName = elementName;
        }
    }

    private interface ElementHandler {
        void start(String name, Attributes attributes, Context context);

        ElementHandler pushChild(String name, Context context);

        void handleText(String text, Context context);

        void finish(Context context);
    }

    private static class NoOpElementHandler implements ElementHandler {
        public ElementHandler pushChild(String name, Context context) {
            return this;
        }

        public void handleText(String text, Context context) {
        }

        public void start(String name, Attributes attributes, Context context) {
        }

        public void finish(Context context) {
        }
    }

    private static abstract class AbstractElementHandler implements ElementHandler {
        protected BuildableInlineContainer attachCrossReference(Context context, final String target) {
            final Map<String, Component> components = context.getComponentsById();
            final String fileName = context.getFileName();
            final int lineNumber = context.getLineNumber();
            final int columnNumber = context.getColumnNumber();
            final String elementName = context.getElementName();
            return context.currentInline().addCrossReference(new LinkResolver() {
                public void resolve(LinkResolverContext resolverContext) {
                    Component component = components.get(target);
                    if (component == null) {
                        String message = String.format("<%s>unknown linkend \"%s\" in %s, line %s, column %s</%s>",
                                elementName, target, fileName, lineNumber, columnNumber, elementName);
                        resolverContext.error(message);
                    } else {
                        resolverContext.crossReference(component);
                    }
                }
            });
        }
    }

    private static class DefaultElementHandler extends AbstractElementHandler {
        public void start(String name, Attributes attributes, Context context) {
        }

        public ElementHandler pushChild(String name, Context context) {
            String message = String.format("<%s>%s, line %s, column %s</%s>", name, context.getFileName(),
                    context.getLineNumber(), context.getColumnNumber(), name);
            context.currentContainer().addError(message);
            return new NoOpElementHandler();
        }

        public void handleText(String text, Context context) {
            if (text.matches("\\s+")) {
                return;
            }
            String message = String.format("(text %s, line %s, column %s)", context.getFileName(),
                    context.getLineNumber(), context.getColumnNumber());
            context.currentContainer().addError(message);
        }

        public void finish(Context context) {
        }
    }

    private static class RootHandler extends DefaultElementHandler {
        @Override
        public ElementHandler pushChild(String name, Context context) {
            if (name.equals("book")) {
                return new BookHandler();
            }
            return super.pushChild(name, context);
        }
    }

    private abstract static class ComponentHandler extends DefaultElementHandler {
        protected abstract BuildableComponent create(Context context);

        @Override
        public void start(String name, Attributes attributes, Context context) {
            BuildableComponent container = create(context);
            String id = attributes.getValue("id");
            if (id != null) {
                container.setId(id);
                context.getComponentsById().put(id, container);
            }
            context.pushContainer(container);
        }

        @Override
        public ElementHandler pushChild(String name, Context context) {
            if (name.equals("title")) {
                return new TitleHandler();
            }
            return super.pushChild(name, context);
        }

        @Override
        public void finish(Context context) {
            context.popContainer();
        }
    }

    private static class BookHandler extends ComponentHandler {
        @Override
        protected BuildableComponent create(Context context) {
            return context.currentComponent();
        }

        @Override
        public ElementHandler pushChild(String name, Context context) {
            if (name.equals("bookinfo")) {
                return new BookInfoHandler();
            }
            if (name.equals("part")) {
                return new PartHandler();
            }
            if (name.equals("chapter")) {
                return new ChapterHandler();
            }
            if (name.equals("appendix")) {
                return new AppendixHandler();
            }
            return super.pushChild(name, context);
        }
    }

    private static class BookInfoHandler extends DefaultElementHandler {
        @Override
        public ElementHandler pushChild(String name, Context context) {
            if (name.equals("title")) {
                return new TitleHandler();
            }
            return super.pushChild(name, context);
        }
    }

    private static class PartHandler extends ComponentHandler {
        @Override
        protected BuildableComponent create(Context context) {
            return context.currentComponent().addPart();
        }

        @Override
        public ElementHandler pushChild(String name, Context context) {
            if (name.equals("chapter")) {
                return new ChapterHandler();
            }
            if (name.equals("appendix")) {
                return new AppendixHandler();
            }
            return super.pushChild(name, context);
        }
    }

    private static class ContainerHandler extends DefaultElementHandler {
        public ElementHandler pushChild(String name, Context context) {
            if (name.equals("para")) {
                return new ParaHandler();
            }
            if (name.equals("itemizedlist")) {
                return new ItemizedListHandler();
            }
            if (name.equals("orderedlist")) {
                return new OrderedListHandler();
            }
            return super.pushChild(name, context);
        }
    }

    private static class SectionHandler extends ContainerHandler {
        protected BuildableComponent create(Context context) {
            return context.currentComponent().addSection();
        }

        @Override
        public void start(String name, Attributes attributes, Context context) {
            BuildableComponent container = create(context);
            String id = attributes.getValue("id");
            if (id != null) {
                container.setId(id);
                context.getComponentsById().put(id, container);
            }
            context.pushContainer(container);
        }

        @Override
        public ElementHandler pushChild(String name, Context context) {
            if (name.equals("title")) {
                return new TitleHandler();
            }
            if (name.equals("section")) {
                return new SectionHandler();
            }
            return super.pushChild(name, context);
        }

        @Override
        public void finish(Context context) {
            context.popContainer();
        }
    }

    private static class ChapterHandler extends SectionHandler {
        @Override
        protected BuildableComponent create(Context context) {
            return context.currentComponent().addChapter();
        }
    }

    private static class AppendixHandler extends SectionHandler {
        @Override
        protected BuildableComponent create(Context context) {
            return context.currentComponent().addAppendix();
        }
    }

    private abstract static class ListHandler extends DefaultElementHandler {
        private BuildableList list;

        abstract BuildableList create(Context context);

        @Override
        public void start(String name, Attributes attributes, Context context) {
            list = create(context);
        }

        @Override
        public ElementHandler pushChild(String name, Context context) {
            if (name.equals("listitem")) {
                return new ListItemHandler(list);
            }
            return super.pushChild(name, context);
        }
    }

    private static class ItemizedListHandler extends ListHandler {
        @Override
        BuildableList create(Context context) {
            return context.currentContainer().addItemisedList();
        }
    }

    private static class OrderedListHandler extends ListHandler {
        @Override
        BuildableList create(Context context) {
            return context.currentContainer().addOrderedList();
        }
    }

    private static class ListItemHandler extends ContainerHandler {
        private final BuildableList list;

        public ListItemHandler(BuildableList list) {
            this.list = list;
        }

        @Override
        public void start(String name, Attributes attributes, Context context) {
            context.pushContainer(list.addItem());
        }

        @Override
        public void finish(Context context) {
            context.popContainer();
        }
    }

    private static class DefaultInlineHandler extends AbstractElementHandler {
        public void start(String name, Attributes attributes, Context context) {
        }

        public ElementHandler pushChild(String name, Context context) {
            String message = String.format("<%s>%s, line %s, column %s</%s>", name, context.getFileName(),
                    context.getLineNumber(), context.getColumnNumber(), name);
            context.currentInline().addError(message);
            return new NoOpElementHandler();
        }

        public void handleText(String text, Context context) {
            context.currentInline().append(text);
        }

        public void finish(Context context) {
        }
    }

    private static class InlineHandler extends DefaultInlineHandler {
        public ElementHandler pushChild(String name, Context context) {
            if (name.equals("code")) {
                return new CodeHandler();
            }
            if (name.equals("literal")) {
                return new LiteralHandler();
            }
            if (name.equals("emphasis")) {
                return new EmphasisHandler();
            }
            if (name.equals("xref")) {
                return new XrefHandler();
            }
            if (name.equals("link")) {
                return new LinkHandler();
            }
            if (name.equals("ulink")) {
                return new UlinkHandler();
            }
            return super.pushChild(name, context);
        }
    }

    private static abstract class TextOnlyInlineHandler extends DefaultInlineHandler {
        abstract BuildableInlineContainer create(Context context, Attributes attributes);

        @Override
        public void start(String name, Attributes attributes, Context context) {
            context.pushInline(create(context, attributes));
        }

        @Override
        public void finish(Context context) {
            context.popInline();
        }
    }

    private static class XrefHandler extends DefaultElementHandler {
        @Override
        public void start(String name, Attributes attributes, final Context context) {
            final String target = attributes.getValue("linkend");
            if (target == null || target.isEmpty()) {
                String message = String.format("<xref>no \"linkend\" attribute specified in %s, line %s, column %s</xref>",
                        context.getFileName(), context.getLineNumber(), context.getColumnNumber());
                context.currentInline().addError(String.format(message));
                return;
            }

            attachCrossReference(context, target);
        }
    }

    private static class LinkHandler extends TextOnlyInlineHandler {
        @Override
        BuildableInlineContainer create(Context context, Attributes attributes) {
            return attachCrossReference(context, attributes.getValue("linkend"));
        }
    }

    private static class UlinkHandler extends TextOnlyInlineHandler {
        @Override
        BuildableInlineContainer create(Context context, Attributes attributes) {
            throw new UnsupportedOperationException();
        }
    }

    private static class CodeHandler extends TextOnlyInlineHandler {
        @Override
        BuildableInlineContainer create(Context context, Attributes attributes) {
            return context.currentInline().addCode();
        }
    }

    private static class LiteralHandler extends TextOnlyInlineHandler {
        @Override
        BuildableInlineContainer create(Context context, Attributes attributes) {
            return context.currentInline().addLiteral();
        }
    }

    private static class EmphasisHandler extends TextOnlyInlineHandler {
        @Override
        BuildableInlineContainer create(Context context, Attributes attributes) {
            return context.currentInline().addEmphasis();
        }
    }

    private static class ParaHandler extends InlineHandler {
        @Override
        public void start(String name, Attributes attributes, Context context) {
            context.pushInline(context.currentContainer().addParagraph());
        }

        @Override
        public void finish(Context context) {
            context.popInline();
        }
    }

    private static class TitleHandler extends InlineHandler {
        @Override
        public void start(String name, Attributes attributes, Context context) {
            context.pushInline(context.currentComponent().getTitle());
        }

        @Override
        public void finish(Context context) {
            context.popInline();
        }
    }
}
