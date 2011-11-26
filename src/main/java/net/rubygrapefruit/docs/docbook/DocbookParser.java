package net.rubygrapefruit.docs.docbook;

import net.rubygrapefruit.docs.model.*;
import net.rubygrapefruit.docs.parser.Parser;

import javax.xml.stream.Location;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.Reader;
import java.util.LinkedList;

/**
 * Builds a document for some Docbook input.
 */
public class DocbookParser extends Parser {
    @Override
    protected Document doParse(Reader input, String fileName) throws XMLStreamException {
        BuildableDocument document = new BuildableDocument();
        XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(input);
        LinkedList<ElementHandler> stack = new LinkedList<ElementHandler>();
        int pos = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf(File.separatorChar));
        if (pos >= 0) {
            fileName = fileName.substring(pos + 1);
        }
        DefaultContext context = new DefaultContext(fileName);
        context.push(document);
        stack.add(new RootHandler());
        try {
            while (reader.hasNext()) {
                XMLEvent event = reader.nextEvent();
                context.location = event.getLocation();
                if (event.isStartElement()) {
                    String elementName = event.asStartElement().getName().getLocalPart();
                    ElementHandler childHandler = stack.getLast().pushChild(elementName, context);
                    stack.add(childHandler);
                    childHandler.start(elementName, context);
                } else if (event.isEndElement()) {
                    stack.removeLast().finish(context);
                } else if (event.isCharacters()) {
                    stack.getLast().handleText(event.asCharacters().getData(), context);
                }
            }
        } finally {
            reader.close();
        }

        return document;
    }

    private interface Context {
        String getFileName();

        int getLineNumber();

        int getColumnNumber();

        BuildableComponent currentComponent();

        BuildableBlockContainer currentContainer();

        BuildableInlineContainer currentInline();

        void setCurrentInline(BuildableInlineContainer container);

        void push(BuildableBlockContainer container);

        void pop();
    }

    private static class DefaultContext implements Context {
        final String fileName;
        final LinkedList<BuildableBlockContainer> containerStack = new LinkedList<BuildableBlockContainer>();
        final LinkedList<BuildableComponent> componentStack = new LinkedList<BuildableComponent>();
        Location location;
        BuildableInlineContainer inlineContainer;

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

        public BuildableComponent currentComponent() {
            return componentStack.getLast();
        }

        public BuildableBlockContainer currentContainer() {
            return containerStack.getLast();
        }

        public BuildableInlineContainer currentInline() {
            return inlineContainer;
        }

        public void setCurrentInline(BuildableInlineContainer container) {
            inlineContainer = container;
        }

        public void push(BuildableBlockContainer container) {
            containerStack.add(container);
            if (container instanceof BuildableComponent) {
                componentStack.add((BuildableComponent) container);
            }
        }

        public void pop() {
            BuildableBlockContainer old = containerStack.removeLast();
            if (old == componentStack.getLast()) {
                componentStack.removeLast();
            }
        }
    }

    private interface ElementHandler {
        void start(String name, Context context);

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

        public void start(String name, Context context) {
        }

        public void finish(Context context) {
        }
    }

    private static class DefaultElementHandler implements ElementHandler {
        public void start(String name, Context context) {
        }

        public ElementHandler pushChild(String name, Context context) {
            String message = String.format("<%s>%s, line %s, column %s</%s>", name, context.getFileName(),
                    context.getLineNumber(), context.getColumnNumber(), name);
            context.currentContainer().addUnknown(message);
            return new NoOpElementHandler();
        }

        public void handleText(String text, Context context) {
            if (text.matches("\\s+")) {
                return;
            }
            String message = String.format("(text %s, line %s, column %s)", context.getFileName(),
                    context.getLineNumber(), context.getColumnNumber());
            context.currentContainer().addUnknown(message);
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
        public void start(String name, Context context) {
            context.push(create(context));
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
            context.pop();
        }
    }

    private static class BookHandler extends ComponentHandler {
        @Override
        protected BuildableComponent create(Context context) {
            return context.currentComponent();
        }

        @Override
        public ElementHandler pushChild(String name, Context context) {
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
        public void start(String name, Context context) {
            context.push(create(context));
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
            context.pop();
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
        public void start(String name, Context context) {
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
        public void start(String name, Context context) {
            context.push(list.addItem());
        }

        @Override
        public void finish(Context context) {
            context.pop();
        }
    }

    private static class InlineHandler implements ElementHandler {
        public void start(String name, Context context) {
        }

        public ElementHandler pushChild(String name, Context context) {
            String message = String.format("<%s>%s, line %s, column %s</%s>", name, context.getFileName(),
                    context.getLineNumber(), context.getColumnNumber(), name);
            context.currentInline().addUnknown(message);
            return new NoOpElementHandler();
        }

        public void handleText(String text, Context context) {
            context.currentInline().append(text);
        }

        public void finish(Context context) {
        }
    }

    private static class ParaHandler extends InlineHandler {
        @Override
        public void start(String name, Context context) {
            context.setCurrentInline(context.currentContainer().addParagraph());
        }

        @Override
        public void finish(Context context) {
            context.setCurrentInline(null);
        }
    }

    private static class TitleHandler extends InlineHandler {
        @Override
        public void start(String name, Context context) {
            context.setCurrentInline(context.currentComponent().getTitle());
        }

        @Override
        public void finish(Context context) {
            context.setCurrentInline(null);
        }
    }
}
