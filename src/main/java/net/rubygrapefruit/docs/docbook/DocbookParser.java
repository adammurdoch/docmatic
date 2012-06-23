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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Builds a document for some Docbook input.
 */
public class DocbookParser extends Parser {
    private final NoOpElementHandler noOpElementHandler = new NoOpElementHandler();
    private final LinkHandlerFactory linkHandlerFactory = new LinkHandlerFactory();
    private final UlinkHandlerFactory ulinkHandlerFactory = new UlinkHandlerFactory();
    private final InlineHandlerFactory inlineHandlerFactory = new InlineHandlerFactory();
    private final BlockHandlerFactory blockHandlerFactory = new BlockHandlerFactory();
    private final XrefHandler xrefHandler = new XrefHandler();
    private final BookHandler bookHandler = new BookHandler();
    private final TitleHandler titleHandler = new TitleHandler();
    private final BookInfoHandler bookInfoHandler = new BookInfoHandler();
    private final PartHandler partHandler = new PartHandler();
    private final ChapterHandler chapterHandler = new ChapterHandler();
    private final AppendixHandler appendixHandler = new AppendixHandler();
    private final ParaHandler paraHandler = new ParaHandler();
    private final SectionHandler sectionHandler = new SectionHandler();
    private final CodeHandler codeHandler = new CodeHandler();
    private final LiteralHandler literalHandler = new LiteralHandler();
    private final EmphasisHandler emphasisHandler = new EmphasisHandler();
    private final ClassNameHandler classNameHandler = new ClassNameHandler();
    private final ExampleHandler exampleHandler = new ExampleHandler();

    @Override
    protected void doParse(Reader input, String fileName, BuildableDocument document) throws Exception {
        final LinkedList<ElementHandler> handlerStack = new LinkedList<ElementHandler>();
        final LinkedList<String> elementNameStack = new LinkedList<String>();
        int pos = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf(File.separatorChar));
        if (pos >= 0) {
            fileName = fileName.substring(pos + 1);
        }
        final DefaultContext context = new DefaultContext(fileName);
        context.push(document);
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
                ElementHandler childHandler = handlerStack.getLast().pushChild(elementName, attributes, context);
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

    private static String normalise(String value) {
        if (value == null) {
            return null;
        }
        String normalised = value.trim();
        if (normalised.isEmpty()) {
            return null;
        }
        return normalised;
    }

    private interface Context {
        String getFileName();

        int getLineNumber();

        int getColumnNumber();

        BuildableComponent currentComponent();

        BuildableBlockContainer ownerContainer();

        BuildableBlockContainer currentContainer();

        BuildableTitledBlockContainer currentTitled();

        BuildableInlineContainer currentInline();

        void push(Object element);

        void pop();

        Map<String, Component> getComponentsById();

        String getElementName();
    }

    private static class DefaultContext implements Context {
        final String fileName;
        final LinkedList<Object> stack = new LinkedList<Object>();
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

        public BuildableBlockContainer ownerContainer() {
            for (Object element : stack) {
                if (element instanceof BuildableBlockContainer) {
                    return (BuildableBlockContainer) element;
                }
            }
            throw new IllegalStateException("No container available.");
        }

        public BuildableComponent currentComponent() {
            return (BuildableComponent) stack.getFirst();
        }

        public BuildableBlockContainer currentContainer() {
            return (BuildableBlockContainer) stack.getFirst();
        }

        public BuildableInlineContainer currentInline() {
            return (BuildableInlineContainer) stack.getFirst();
        }

        public BuildableTitledBlockContainer currentTitled() {
            return (BuildableTitledBlockContainer) stack.getFirst();
        }

        public void push(Object element) {
            stack.addFirst(element);
        }

        public void pop() {
            stack.removeFirst();
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

        ElementHandler pushChild(String name, Attributes attributes, Context context);

        void handleText(String text, Context context);

        void finish(Context context);
    }

    private static class NoOpElementHandler implements ElementHandler {
        public ElementHandler pushChild(String name, Attributes attributes, Context context) {
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

        protected BuildableInlineContainer attachLink(Context context, final String target) {
            final String fileName = context.getFileName();
            final int lineNumber = context.getLineNumber();
            final int columnNumber = context.getColumnNumber();
            final String elementName = context.getElementName();
            return context.currentInline().addCrossReference(new LinkResolver() {
                public void resolve(LinkResolverContext resolverContext) {
                    URI url;
                    try {
                        url = new URI(target);
                    } catch (URISyntaxException e) {
                        String message = String.format("<%s>badly formed URL \"%s\" specified in %s, line %s, column %s</%s>",
                                elementName, target, fileName, lineNumber, columnNumber, elementName);
                        resolverContext.error(message);
                        return;
                    }
                    resolverContext.url(url);
                }
            });
        }
    }

    private class DefaultElementHandler extends AbstractElementHandler {
        public void start(String name, Attributes attributes, Context context) {
        }

        public ElementHandler pushChild(String name, Attributes attributes, Context context) {
            String message = String.format("<%s>%s, line %s, column %s</%s>", name, context.getFileName(),
                    context.getLineNumber(), context.getColumnNumber(), name);
            context.ownerContainer().addError(message);
            return noOpElementHandler;
        }

        public void handleText(String text, Context context) {
            if (text.matches("\\s+")) {
                return;
            }
            String message = String.format("(text %s, line %s, column %s)", context.getFileName(),
                    context.getLineNumber(), context.getColumnNumber());
            context.ownerContainer().addError(message);
        }

        public void finish(Context context) {
        }
    }

    private class RootHandler extends DefaultElementHandler {
        @Override
        public ElementHandler pushChild(String name, Attributes attributes, Context context) {
            if (name.equals("book")) {
                return bookHandler;
            }
            return super.pushChild(name, attributes, context);
        }
    }

    private abstract class ComponentHandler extends DefaultElementHandler {
        protected abstract BuildableComponent create(Context context);

        @Override
        public void start(String name, Attributes attributes, Context context) {
            BuildableComponent container = create(context);
            String id = attributes.getValue("id");
            if (id != null) {
                container.setId(id);
                context.getComponentsById().put(id, container);
            }
            context.push(container);
        }

        @Override
        public ElementHandler pushChild(String name, Attributes attributes, Context context) {
            if (name.equals("title")) {
                return titleHandler;
            }
            return super.pushChild(name, attributes, context);
        }

        @Override
        public void finish(Context context) {
            context.pop();
        }
    }

    private class BookHandler extends ComponentHandler {
        @Override
        protected BuildableComponent create(Context context) {
            return context.currentComponent();
        }

        @Override
        public ElementHandler pushChild(String name, Attributes attributes, Context context) {
            if (name.equals("bookinfo")) {
                return bookInfoHandler;
            }
            if (name.equals("part")) {
                return partHandler;
            }
            if (name.equals("chapter")) {
                return chapterHandler;
            }
            if (name.equals("appendix")) {
                return appendixHandler;
            }
            return super.pushChild(name, attributes, context);
        }
    }

    private class BookInfoHandler extends DefaultElementHandler {
        @Override
        public ElementHandler pushChild(String name, Attributes attributes, Context context) {
            if (name.equals("title")) {
                return titleHandler;
            }
            return super.pushChild(name, attributes, context);
        }
    }

    private class PartHandler extends ComponentHandler {
        @Override
        protected BuildableComponent create(Context context) {
            return context.currentComponent().addPart();
        }

        @Override
        public ElementHandler pushChild(String name, Attributes attributes, Context context) {
            if (name.equals("chapter")) {
                return chapterHandler;
            }
            if (name.equals("appendix")) {
                return appendixHandler;
            }
            return super.pushChild(name, attributes, context);
        }
    }

    private class BlockHandlerFactory {
        public ElementHandler createHandler(String name, Attributes attributes, Context context) {
            if (name.equals("para")) {
                return paraHandler;
            }
            if (name.equals("itemizedlist")) {
                return new ItemizedListHandler();
            }
            if (name.equals("orderedlist")) {
                return new OrderedListHandler();
            }
            if (name.equals("programlisting")) {
                return new ProgramListingHandler();
            }
            return null;
        }
    }

    private class ContainerHandler extends DefaultElementHandler {
        public ElementHandler pushChild(String name, Attributes attributes, Context context) {
            ElementHandler handler = blockHandlerFactory.createHandler(name, attributes, context);
            if (handler != null) {
                return handler;
            }
            if (name.equals("example")) {
                return exampleHandler;
            }
            return super.pushChild(name, attributes, context);
        }
    }

    private class SectionHandler extends ContainerHandler {
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
            context.push(container);
        }

        @Override
        public ElementHandler pushChild(String name, Attributes attributes, Context context) {
            if (name.equals("title")) {
                return titleHandler;
            }
            if (name.equals("section")) {
                return sectionHandler;
            }
            return super.pushChild(name, attributes, context);
        }

        @Override
        public void finish(Context context) {
            context.pop();
        }
    }

    private class ChapterHandler extends SectionHandler {
        @Override
        protected BuildableComponent create(Context context) {
            return context.currentComponent().addChapter();
        }
    }

    private class AppendixHandler extends SectionHandler {
        @Override
        protected BuildableComponent create(Context context) {
            return context.currentComponent().addAppendix();
        }
    }

    private abstract class ListHandler extends DefaultElementHandler {
        private BuildableList list;

        abstract BuildableList create(Context context);

        @Override
        public void start(String name, Attributes attributes, Context context) {
            list = create(context);
        }

        @Override
        public ElementHandler pushChild(String name, Attributes attributes, Context context) {
            if (name.equals("listitem")) {
                return new ListItemHandler(list);
            }
            return super.pushChild(name, attributes, context);
        }
    }

    private class ItemizedListHandler extends ListHandler {
        @Override
        BuildableList create(Context context) {
            return context.currentContainer().addItemisedList();
        }
    }

    private class OrderedListHandler extends ListHandler {
        @Override
        BuildableList create(Context context) {
            return context.currentContainer().addOrderedList();
        }
    }

    private class ListItemHandler extends ContainerHandler {
        private final BuildableList list;

        public ListItemHandler(BuildableList list) {
            this.list = list;
        }

        @Override
        public void start(String name, Attributes attributes, Context context) {
            context.push(list.addItem());
        }

        @Override
        public void finish(Context context) {
            context.pop();
        }
    }

    private class ExampleHandler extends DefaultElementHandler {
        @Override
        public void start(String name, Attributes attributes, Context context) {
            context.push(context.currentContainer().addExample());
        }

        @Override
        public ElementHandler pushChild(String name, Attributes attributes, Context context) {
            ElementHandler handler = blockHandlerFactory.createHandler(name, attributes, context);
            if (handler != null) {
                return handler;
            }
            if (name.equals("title")) {
                return titleHandler;
            }
            return super.pushChild(name, attributes, context);
        }

        @Override
        public void finish(Context context) {
            context.pop();
        }
    }

    private class ProgramListingHandler extends DefaultElementHandler {
        private BuildableProgramListing programListing;

        @Override
        public void start(String name, Attributes attributes, Context context) {
            programListing = context.currentContainer().addProgramListing();
        }

        @Override
        public void handleText(String text, Context context) {
            programListing.append(text);
        }
    }

    private class DefaultInlineHandler extends AbstractElementHandler {
        public void start(String name, Attributes attributes, Context context) {
        }

        public ElementHandler pushChild(String name, Attributes attributes, Context context) {
            String message = String.format("<%s>%s, line %s, column %s</%s>", name, context.getFileName(),
                    context.getLineNumber(), context.getColumnNumber(), name);
            context.currentInline().addError(message);
            return noOpElementHandler;
        }

        public void handleText(String text, Context context) {
            context.currentInline().append(text);
        }

        public void finish(Context context) {
        }
    }

    private interface ElementHandlerFactory {
        ElementHandler createHandler(String name, Attributes attributes, Context context);
    }

    private class InlineHandlerFactory {
        ElementHandler createHandler(String name, Attributes attributes, Context context) {
            if (name.equals("code")) {
                return codeHandler;
            }
            if (name.equals("literal")) {
                return literalHandler;
            }
            if (name.equals("emphasis")) {
                return emphasisHandler;
            }
            if (name.equals("classname")) {
                return classNameHandler;
            }
            return null;
        }
    }

    private class InlineContainerHandler extends DefaultInlineHandler {
        public ElementHandler pushChild(String name, Attributes attributes, Context context) {
            ElementHandler handler = inlineHandlerFactory.createHandler(name, attributes, context);
            if (handler != null) {
                return handler;
            }
            if (name.equals("xref")) {
                return xrefHandler;
            }
            if (name.equals("link")) {
                return linkHandlerFactory.createHandler(name, attributes, context);
            }
            if (name.equals("ulink")) {
                return ulinkHandlerFactory.createHandler(name, attributes, context);
            }
            return super.pushChild(name, attributes, context);
        }
    }

    private abstract class InlineHandler extends DefaultInlineHandler {
        abstract BuildableInlineContainer create(Context context, Attributes attributes);

        @Override
        public void start(String name, Attributes attributes, Context context) {
            context.push(create(context, attributes));
        }

        @Override
        public void finish(Context context) {
            context.pop();
        }

        @Override
        public ElementHandler pushChild(String name, Attributes attributes, Context context) {
            ElementHandler handler = inlineHandlerFactory.createHandler(name, attributes, context);
            if (handler != null) {
                return handler;
            }
            return super.pushChild(name, attributes, context);
        }
    }

    private class XrefHandler extends DefaultElementHandler {
        @Override
        public void start(String name, Attributes attributes, final Context context) {
            final String target = normalise(attributes.getValue("linkend"));
            if (target != null) {
                attachCrossReference(context, target);
                return;
            }

            String message = String.format("<xref>no \"linkend\" attribute specified in %s, line %s, column %s</xref>",
                    context.getFileName(), context.getLineNumber(), context.getColumnNumber());
            context.currentInline().addError(String.format(message));
        }
    }

    private class LinkHandlerFactory implements ElementHandlerFactory {
        public ElementHandler createHandler(String name, Attributes attributes, Context context) {
            String linkend = normalise(attributes.getValue("linkend"));
            String href = normalise(attributes.getValue("http://www.w3.org/1999/xlink", "href"));
            if (linkend == null && href == null) {
                String message = String.format(
                        "<link>no \"linkend\" or \"href\" attribute specified in %s, line %s, column %s</link>",
                        context.getFileName(), context.getLineNumber(), context.getColumnNumber());
                context.currentInline().addError(String.format(message));
                return new NoOpElementHandler();
            }
            if (linkend != null && href != null) {
                String message = String.format(
                        "<link>both \"linkend\" and \"href\" attribute specified in %s, line %s, column %s</link>",
                        context.getFileName(), context.getLineNumber(), context.getColumnNumber());
                context.currentInline().addError(String.format(message));
                return new NoOpElementHandler();
            }

            if (linkend != null) {
                return new CrossReferenceHandler(linkend);
            }
            return new LinkHandler(href);
        }
    }

    private class CrossReferenceHandler extends InlineHandler {
        private final String linkend;

        public CrossReferenceHandler(String linkend) {
            this.linkend = linkend;
        }

        @Override
        BuildableInlineContainer create(Context context, Attributes attributes) {
            return attachCrossReference(context, linkend);
        }
    }

    private class UlinkHandlerFactory implements ElementHandlerFactory {
        public ElementHandler createHandler(String name, Attributes attributes, Context context) {
            String href = normalise(attributes.getValue("url"));
            if (href != null) {
                return new LinkHandler(href);
            }
            String message = String.format("<ulink>no \"url\" attribute specified in %s, line %s, column %s</ulink>",
                    context.getFileName(), context.getLineNumber(), context.getColumnNumber());
            context.currentInline().addError(String.format(message));
            return noOpElementHandler;
        }
    }

    private class LinkHandler extends InlineHandler {
        private final String href;

        public LinkHandler(String href) {
            this.href = href;
        }

        @Override
        BuildableInlineContainer create(Context context, Attributes attributes) {
            return attachLink(context, href);
        }
    }

    private class CodeHandler extends InlineHandler {
        @Override
        BuildableInlineContainer create(Context context, Attributes attributes) {
            return context.currentInline().addCode();
        }
    }

    private class LiteralHandler extends InlineHandler {
        @Override
        BuildableInlineContainer create(Context context, Attributes attributes) {
            return context.currentInline().addLiteral();
        }
    }

    private class EmphasisHandler extends InlineHandler {
        @Override
        BuildableInlineContainer create(Context context, Attributes attributes) {
            return context.currentInline().addEmphasis();
        }
    }

    private class ClassNameHandler extends InlineHandler {
        @Override
        BuildableInlineContainer create(Context context, Attributes attributes) {
            return context.currentInline().addClassName();
        }
    }

    private class ParaHandler extends InlineContainerHandler {
        @Override
        public void start(String name, Attributes attributes, Context context) {
            context.push(context.currentContainer().addParagraph());
        }

        @Override
        public void finish(Context context) {
            context.pop();
        }
    }

    private class TitleHandler extends InlineContainerHandler {
        @Override
        public void start(String name, Attributes attributes, Context context) {
            context.push(context.currentTitled().getTitle());
        }

        @Override
        public void finish(Context context) {
            context.pop();
        }
    }
}
