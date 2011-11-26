package net.rubygrapefruit.docs.pdf;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import net.rubygrapefruit.docs.model.*;
import net.rubygrapefruit.docs.model.Document;
import net.rubygrapefruit.docs.model.List;
import net.rubygrapefruit.docs.model.ListItem;
import net.rubygrapefruit.docs.model.Paragraph;
import net.rubygrapefruit.docs.model.Section;
import net.rubygrapefruit.docs.renderer.Renderer;
import net.rubygrapefruit.docs.renderer.TextTheme;
import net.rubygrapefruit.docs.renderer.Theme;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;

public class PdfRenderer extends Renderer {
    private Font base;
    private java.util.List<Font> headerFonts = new ArrayList<Font>();
    private Font unknown;
    private BigDecimal lineHeight;

    @Override
    protected void doRender(Document document, Theme theme, OutputStream stream) throws Exception {
        TextTheme textTheme = theme.asTextTheme();
        Font.FontFamily fontFamily = Font.FontFamily.TIMES_ROMAN;
        BaseColor textColor = BaseColor.BLACK;
        lineHeight = BigDecimal.valueOf(14, 1);
        if (textTheme != null) {
            fontFamily = textTheme.getFontName().equals("sans-serif") ? Font.FontFamily.HELVETICA
                    : Font.FontFamily.TIMES_ROMAN;
            textColor = new BaseColor(textTheme.getColour());
            lineHeight = textTheme.getLineSpacing();
        }

        // TODO - theme font sizes
        base = new Font(fontFamily, 12, Font.NORMAL, textColor);
        headerFonts.clear();
        headerFonts.add(new Font(fontFamily, 22, Font.BOLD, textColor));
        headerFonts.add(new Font(fontFamily, 16, Font.BOLD, textColor));
        headerFonts.add(new Font(fontFamily, 14, Font.BOLD, textColor));
        headerFonts.add(new Font(fontFamily, 12, Font.BOLD, textColor));
        unknown = new Font(fontFamily, 12, Font.NORMAL, BaseColor.RED);

        // TODO - theme margins
        com.itextpdf.text.Document pdfDocument = new com.itextpdf.text.Document(PageSize.A4, 40, 20, 40, 20);
        PdfWriter.getInstance(pdfDocument, stream);
        pdfDocument.open();
        writeComponent(document, 0, pdfDocument);
        pdfDocument.close();
    }

    private void writeComponent(Component component, int depth, com.itextpdf.text.Document target)
            throws DocumentException {
        writeTitle(component, 0, target);
        for (Block block : component.getContents()) {
            if (block instanceof Section) {
                Section child = (Section) block;
                writeSection(child, depth == 0 ? 0 : 1, target);
            } else if (block instanceof Component) {
                Component child = (Component) block;
                writeComponent(child, depth + 1, target);
            } else {
                target.add(convertBlock(block));
            }
        }
    }

    private void writeSection(Section section, int depth, com.itextpdf.text.Document target)
            throws DocumentException {
        writeTitle(section, depth, target);
        for (Block block : section.getContents()) {
            if (block instanceof Section) {
                Section child = (Section) block;
                writeSection(child, depth + 1, target);
            } else {
                target.add(convertBlock(block));
            }
        }
    }

    private void writeTitle(Component component, int depth, com.itextpdf.text.Document target)
            throws DocumentException {
        if (component.getTitle().isEmpty()) {
            return;
        }

        com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph();
        title.setFont(depth < headerFonts.size() ? headerFonts.get(depth) : headerFonts.get(headerFonts.size() - 1));
        writeContents(component.getTitle(), title);

        // TODO - theme spacing
        title.setSpacingBefore(15);
        title.setSpacingAfter(8);
        target.add(title);
    }

    private Element convertBlock(Block block) throws DocumentException {
        if (block instanceof Paragraph) {
            Paragraph paragraph = (Paragraph) block;
            com.itextpdf.text.Paragraph pdfParagraph = new com.itextpdf.text.Paragraph();
            pdfParagraph.setFont(base);
            pdfParagraph.setAlignment(Element.ALIGN_JUSTIFIED);
            // TODO - theme spacing
            pdfParagraph.setSpacingBefore(4);
            pdfParagraph.setSpacingAfter(4);
            pdfParagraph.setMultipliedLeading(lineHeight.floatValue());
            writeContents(paragraph, pdfParagraph);
            return pdfParagraph;
        } else if (block instanceof ItemisedList) {
            ItemisedList list = (ItemisedList) block;
            // TODO - theme indent
            com.itextpdf.text.List pdfList = new com.itextpdf.text.List(false, 24);
            pdfList.setListSymbol("\u2022   ");
            addItems(list, pdfList);
            return pdfList;
        } else if (block instanceof OrderedList) {
            OrderedList list = (OrderedList) block;
            // TODO - theme indent
            com.itextpdf.text.List pdfList = new com.itextpdf.text.List(true, 24);
            addItems(list, pdfList);
            return pdfList;
        } else if (block instanceof Unknown) {
            Unknown unknown = (Unknown) block;
            com.itextpdf.text.Paragraph paragraph = new com.itextpdf.text.Paragraph();
            paragraph.setFont(this.unknown);
            paragraph.add(unknown.getMessage());
            return paragraph;
        } else {
            throw new IllegalStateException(String.format("Don't know how to render block of type '%s'.",
                    block.getClass().getSimpleName()));
        }
    }

    private void writeContents(InlineContainer inlineContainer, com.itextpdf.text.Paragraph paragraph) {
        for (Inline inline : inlineContainer.getContents()) {
            if (inline instanceof Text) {
                Text text = (Text) inline;
                paragraph.add(text.getText());
            } else if (inline instanceof Unknown) {
                Unknown unknown = (Unknown) inline;
                Chunk chunk = new Chunk(unknown.getMessage(), this.unknown);
                paragraph.add(chunk);
            } else {
                throw new IllegalStateException(String.format("Don't know how to render inline of type '%s'.",
                        inline.getClass().getSimpleName()));
            }
        }
    }

    private void addItems(List list, com.itextpdf.text.List pdfList) throws DocumentException {
        for (ListItem item : list.getItems()) {
            com.itextpdf.text.ListItem pdfItem = new com.itextpdf.text.ListItem();
            pdfList.add(pdfItem);

            // TODO - theme spacing
            pdfItem.setSpacingBefore(4);
            pdfItem.setSpacingAfter(4);
            for (Block childBlock : item.getContents()) {
                pdfItem.add(convertBlock(childBlock));
            }
            pdfItem.getListSymbol().setFont(base);
        }
    }
}
