package net.rubygrapefruit.docs.docbook;

import net.rubygrapefruit.docs.model.*;
import net.rubygrapefruit.docs.parser.Parser;

import javax.xml.stream.Location;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.Reader;
import java.util.LinkedList;

/**
 * Builds a document for some Docbook input.
 */
public class DocbookParser extends Parser {
    @Override
    protected Document doParse(Reader input, String fileName) throws XMLStreamException {
        BuildableDocument document = new BuildableDocument();
        DocumentBuilder builder = new DocumentBuilder(document);
        XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(input);
        LinkedList<ElementHandler> stack = new LinkedList<ElementHandler>();
        DefaultContext context = new DefaultContext(builder, fileName);
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
        
        DocumentBuilder getBuilder();
    }
    
    private static class DefaultContext implements Context {
        final String fileName;
        final DocumentBuilder builder;
        Location location;
        
        private DefaultContext(DocumentBuilder builder, String fileName) {
            this.builder = builder;
            this.fileName = fileName;
        }

        public DocumentBuilder getBuilder() {
            return builder;
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
            context.getBuilder().appendUnknown(String.format("<%s>", name), context.getFileName(),
                    context.getLineNumber(), context.getColumnNumber());
            return new NoOpElementHandler();
        }

        public void handleText(String text, Context context) {
            if (text.matches("\\s+")) {
                return;
            }
            context.getBuilder().appendUnknown("text", context.getFileName(), context.getLineNumber(),
                    context.getColumnNumber());
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

    private static class BookHandler extends DefaultElementHandler {
        @Override
        public ElementHandler pushChild(String name, Context context) {
            if (name.equals("chapter")) {
                return new SectionHandler();
            }
            return super.pushChild(name, context);
        }
    }

    private static class ParaHandler extends DefaultElementHandler {
        private final BuildableContainer container;
        private BuildableParagraph paragraph;

        private ParaHandler(BuildableContainer container) {
            this.container = container;
        }

        @Override
        public void start(String name, Context context) {
            paragraph = container.addParagraph();
        }

        @Override
        public void handleText(String text, Context context) {
            paragraph.append(text);
        }
    }

    private static class ContainerHandler extends DefaultElementHandler {
        protected ElementHandler pushChild(String name, Context context, BuildableContainer container) {
            if (name.equals("para")) {
                return new ParaHandler(container);
            }
            if (name.equals("itemizedlist")) {
                return new ItemizedListHandler(container);
            }
            if (name.equals("orderedlist")) {
                return new OrderedListHandler(container);
            }
            return super.pushChild(name, context);
        }
    }

    private static class SectionHandler extends ContainerHandler {
        private BuildableSection section;

        @Override
        public void start(String name, Context context) {
            section = context.getBuilder().appendSection();
        }

        @Override
        public ElementHandler pushChild(String name, Context context) {
            if (name.equals("title")) {
                return new TitleHandler();
            }
            if (name.equals("section")) {
                return new SectionHandler();
            }
            return super.pushChild(name, context, section);
        }

        @Override
        public void finish(Context context) {
            context.getBuilder().popSection();
        }
    }

    private abstract static class ListHandler extends DefaultElementHandler {
        protected final BuildableContainer container;
        private BuildableList list;

        private ListHandler(BuildableContainer container) {
            this.container = container;
        }

        abstract BuildableList create();

        @Override
        public void start(String name, Context context) {
            list = create();
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
        private ItemizedListHandler(BuildableContainer container) {
            super(container);
        }

        @Override
        BuildableList create() {
            return container.addItemisedList();
        }
    }

    private static class OrderedListHandler extends ListHandler {
        private OrderedListHandler(BuildableContainer container) {
            super(container);
        }

        @Override
        BuildableList create() {
            return container.addOrderedList();
        }
    }

    private static class ListItemHandler extends ContainerHandler {
        private final BuildableList list;
        private BuildableListItem item;

        public ListItemHandler(BuildableList list) {
            this.list = list;
        }

        @Override
        public void start(String name, Context context) {
            item = list.addItem();
        }

        @Override
        public ElementHandler pushChild(String name, Context context) {
            return pushChild(name, context, item);
        }
    }

    private static class TitleHandler extends DefaultElementHandler {
        private final StringBuilder content = new StringBuilder();

        @Override
        public void handleText(String text, Context context) {
            content.append(text);
        }

        @Override
        public void finish(Context context) {
            context.getBuilder().getCurrentComponent().setTitle(content);
        }
    }
}
