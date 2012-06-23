package net.rubygrapefruit.docs.pdf;

import com.itextpdf.text.*;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfWriter;
import net.rubygrapefruit.docs.model.*;
import net.rubygrapefruit.docs.model.Error;
import net.rubygrapefruit.docs.model.List;
import net.rubygrapefruit.docs.model.ListItem;
import net.rubygrapefruit.docs.model.Paragraph;
import net.rubygrapefruit.docs.model.Section;
import net.rubygrapefruit.docs.renderer.BuildableChunk;
import net.rubygrapefruit.docs.renderer.RenderableDocument;
import net.rubygrapefruit.docs.renderer.SingleFileRenderer;
import net.rubygrapefruit.docs.renderer.TitleBlock;
import net.rubygrapefruit.docs.theme.TextTheme;
import net.rubygrapefruit.docs.theme.Theme;

import java.io.OutputStream;
import java.math.BigDecimal;

public class PdfRenderer extends SingleFileRenderer {
    private BigDecimal lineHeight;

    @Override
    protected void doRender(RenderableDocument document, Theme theme, OutputStream stream) throws Exception {
        TextTheme textTheme = theme.getAspect(TextTheme.class);
        lineHeight = BigDecimal.valueOf(14, 1);
        if (textTheme != null) {
            lineHeight = textTheme.getLineSpacing();
        }
        FontStack fonts = new FontStack(textTheme);

        // TODO - theme margins
        com.itextpdf.text.Document pdfDocument = new com.itextpdf.text.Document(PageSize.A4, 40, 20, 40, 20);
        PdfWriter.getInstance(pdfDocument, stream);
        pdfDocument.open();
        writeDocument(document, fonts, pdfDocument);
        pdfDocument.close();
    }

    private void writeDocument(RenderableDocument document, FontStack fonts, com.itextpdf.text.Document pdfDocument)
            throws DocumentException {
        boolean first = true;
        for (BuildableChunk chunk : document.getContents()) {
            if (!first) {
                pdfDocument.newPage();
            }
            first = false;
            for (Block block : chunk.getContents()) {
                if (block instanceof Component) {
                    writeComponent((Component) block, fonts, 1, pdfDocument);
                } else if (block instanceof TitleBlock) {
                    writeTitleBlock((TitleBlock) block, fonts, pdfDocument);
                } else if (block instanceof Error) {
                    pdfDocument.add(createErrorBlock((Error) block, fonts));
                } else {
                    writeComponentBlock(block, fonts, pdfDocument);
                }
            }
        }
    }

    private void writeTitleBlock(TitleBlock titleBlock, FontStack fonts, Document pdfDocument) throws DocumentException {
        writeTitle(titleBlock.getComponent(), fonts, 0, pdfDocument);
    }

    private void writeComponent(Component component, FontStack fonts, int depth, com.itextpdf.text.Document target)
            throws DocumentException {
        writeTitle(component, fonts, 0, target);
        for (Block block : component.getContents()) {
            if (block instanceof Section) {
                Section child = (Section) block;
                writeSection(child, fonts, depth == 0 ? 0 : 1, target);
            } else if (block instanceof Component) {
                Component child = (Component) block;
                writeComponent(child, fonts, depth + 1, target);
            } else {
                writeComponentBlock(block, fonts, target);
            }
        }
    }

    private void writeSection(Section section, FontStack fonts, int depth, com.itextpdf.text.Document target)
            throws DocumentException {
        writeTitle(section, fonts, depth, target);
        for (Block block : section.getContents()) {
            if (block instanceof Section) {
                Section child = (Section) block;
                writeSection(child, fonts, depth + 1, target);
            } else {
                writeComponentBlock(block, fonts, target);
            }
        }
    }

    private void writeTitle(Component component, FontStack fonts, int depth, com.itextpdf.text.Document target)
            throws DocumentException {
        if (component.getTitle().isEmpty()) {
            return;
        }

        com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph();
        FontStack headerFonts = fonts.getHeader(depth);
        title.setFont(headerFonts.getBase());
        Anchor anchor = new Anchor();
        anchor.setFont(title.getFont());
        anchor.setName(component.getId());
        title.add(anchor);
        writeContents(component.getTitle(), headerFonts, new AnchorBackedContainer(title, anchor));

        // TODO - theme spacing
        title.setSpacingBefore(15);
        title.setSpacingAfter(8);
        target.add(title);
    }

    private void writeComponentBlock(Block block, FontStack fonts, Document target) throws DocumentException {
        target.add(convertBlock(block, fonts));
    }

    private Element convertBlock(Block block, FontStack fonts) throws DocumentException {
        if (block instanceof Paragraph) {
            Paragraph paragraph = (Paragraph) block;
            com.itextpdf.text.Paragraph pdfParagraph = new com.itextpdf.text.Paragraph();
            pdfParagraph.setFont(fonts.getBase());
            pdfParagraph.setAlignment(Element.ALIGN_JUSTIFIED);
            // TODO - theme spacing
            pdfParagraph.setSpacingBefore(4);
            pdfParagraph.setSpacingAfter(4);
            pdfParagraph.setMultipliedLeading(lineHeight.floatValue());
            writeContents(paragraph, fonts, new PhraseBackedContainer(pdfParagraph));
            return pdfParagraph;
        } else if (block instanceof ItemisedList) {
            ItemisedList list = (ItemisedList) block;
            // TODO - theme indent
            com.itextpdf.text.List pdfList = new com.itextpdf.text.List(false, 24);
            pdfList.setListSymbol("\u2022   ");
            addItems(list, fonts, pdfList);
            return pdfList;
        } else if (block instanceof OrderedList) {
            OrderedList list = (OrderedList) block;
            // TODO - theme indent
            com.itextpdf.text.List pdfList = new com.itextpdf.text.List(true, 24);
            addItems(list, fonts, pdfList);
            return pdfList;
        } else if (block instanceof Error) {
            return createErrorBlock((Error) block, fonts);
        } else {
            throw new IllegalStateException(String.format("Don't know how to render block of type '%s'.",
                    block.getClass().getSimpleName()));
        }
    }

    private Element createErrorBlock(Error error, FontStack fonts) {
        com.itextpdf.text.Paragraph paragraph = new com.itextpdf.text.Paragraph();
        paragraph.setFont(fonts.getError());
        paragraph.add(error.getMessage());
        return paragraph;
    }

    private void writeContents(InlineContainer inlineContainer, FontStack fonts, PhraseContainer owner) {
        for (Inline inline : inlineContainer.getContents()) {
            if (inline instanceof Text) {
                Text text = (Text) inline;
                owner.add(new Chunk(text.getText(), fonts.getBase()));
            } else if (inline instanceof Code || inline instanceof Literal || inline instanceof ClassName) {
                FontStack monospacedFonts = fonts.getMonospaced();
                writeContents((InlineContainer) inline, monospacedFonts, owner);
            } else if (inline instanceof Emphasis) {
                FontStack italicFonts = fonts.getItalic();
                writeContents((InlineContainer) inline, italicFonts, owner);
            } else if (inline instanceof CrossReference) {
                writeCrossReference((CrossReference) inline, fonts, owner);
            } else if (inline instanceof Link) {
                writeLink((Link) inline, fonts, owner);
            } else if (inline instanceof Error) {
                Error error = (Error) inline;
                owner.add(new Chunk(error.getMessage(), fonts.getError()));
            } else {
                throw new IllegalStateException(String.format("Don't know how to render inline of type '%s'.",
                        inline.getClass().getSimpleName()));
            }
        }
    }

    private void writeCrossReference(CrossReference crossReference, FontStack fonts, PhraseContainer owner) {
        Referenceable target = crossReference.getTarget();
        Anchor anchor = new Anchor();
        FontStack linkFonts = fonts.getUnderline();
        anchor.setFont(linkFonts.getBase());
        writeContents(crossReference, fonts, new PhraseBackedContainer(anchor));
        anchor.setReference("#" + target.getId());
        owner.add(anchor);
    }

    private void writeLink(Link link, FontStack fonts, PhraseContainer owner) {
        Anchor anchor = new Anchor();
        FontStack linkFonts = fonts.getUnderline();
        anchor.setFont(linkFonts.getBase());
        writeContents(link, fonts, new PhraseBackedContainer(anchor));
        anchor.setReference(link.getTarget().toString());
        owner.add(anchor);
    }

    private void addItems(List list, FontStack fonts, com.itextpdf.text.List pdfList) throws DocumentException {
        for (ListItem item : list.getItems()) {
            com.itextpdf.text.ListItem pdfItem = new com.itextpdf.text.ListItem();
            pdfList.add(pdfItem);

            // TODO - theme spacing
            pdfItem.setSpacingBefore(4);
            pdfItem.setSpacingAfter(4);
            for (Block childBlock : item.getContents()) {
                pdfItem.add(convertBlock(childBlock, fonts));
            }
            pdfItem.getListSymbol().setFont(fonts.getBase());
        }
    }

    private interface PhraseContainer {
        void add(Chunk chunk);

        void add(Phrase phrase);
    }

    private static class PhraseBackedContainer implements PhraseContainer {
        private final Phrase phrase;

        private PhraseBackedContainer(Phrase phrase) {
            this.phrase = phrase;
        }

        public void add(Chunk chunk) {
            phrase.add(chunk);
        }

        public void add(Phrase phrase) {
            this.phrase.add(phrase);
        }
    }

    private static class AnchorBackedContainer implements PhraseContainer {
        private final Anchor anchor;
        private final Phrase parent;
        private Phrase current;

        private AnchorBackedContainer(Phrase parent, Anchor anchor) {
            this.parent = parent;
            this.anchor = anchor;
            current = anchor;
        }

        public void add(Chunk chunk) {
            current.add(chunk);
        }

        public void add(Phrase phrase) {
            if (phrase instanceof Anchor) {
                Anchor nestedAnchor = (Anchor) phrase;
                parent.add(nestedAnchor);
                current = parent;
            } else {
                current.add(phrase);
            }
        }
    }
}
