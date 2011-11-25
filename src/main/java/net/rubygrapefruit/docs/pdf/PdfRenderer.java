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

public class PdfRenderer extends Renderer {
    private Font base;
    private Font h1;
    private Font h2;
    private Font unknown;

    @Override
    protected void doRender(Document document, Theme theme, OutputStream stream) throws Exception {
        TextTheme textTheme = theme.asTextTheme();
        Font.FontFamily fontFamily = Font.FontFamily.TIMES_ROMAN;
        BaseColor textColor = BaseColor.BLACK;
        if (textTheme != null) {
            fontFamily = textTheme.getFontName().equals("sans-serif") ? Font.FontFamily.HELVETICA : Font.FontFamily.TIMES_ROMAN;
            textColor = new BaseColor(textTheme.getColour());
        }

        // TODO - theme font sizes
        base = new Font(fontFamily, 12, Font.NORMAL, textColor);
        h1 = new Font(fontFamily, 22, Font.BOLD, textColor);
        h2 = new Font(fontFamily, 16, Font.BOLD, textColor);
        unknown = new Font(fontFamily, 12, Font.NORMAL, BaseColor.RED);

        // TODO - theme margins
        com.itextpdf.text.Document pdfDocument = new com.itextpdf.text.Document(PageSize.A4, 40, 20, 40, 20);
        PdfWriter.getInstance(pdfDocument, stream);
        pdfDocument.open();
        writeContents(document, 0, pdfDocument);
        pdfDocument.close();
    }

    private void writeContents(Component component, int depth, com.itextpdf.text.Document target)
            throws DocumentException {
        for (Block block : component.getContents()) {
            if (block instanceof Section) {
                Section child = (Section) block;
                com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph(child.getTitle(),
                        depth == 0 ? h1 : h2);
                // TODO - theme spacing
                title.setSpacingBefore(15);
                title.setSpacingAfter(8);
                target.add(title);
                writeContents(child, depth + 1, target);
            } else {
                target.add(convertBlock(block));
            }
        }
    }

    private Element convertBlock(Block block) throws DocumentException {
        if (block instanceof Paragraph) {
            Paragraph paragraph = (Paragraph) block;
            com.itextpdf.text.Paragraph pdfParagraph = new com.itextpdf.text.Paragraph(paragraph.getText(), base);
            // TODO - theme spacing
            pdfParagraph.setSpacingBefore(2);
            pdfParagraph.setSpacingAfter(2);
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
        } else if (block instanceof UnknownBlock) {
            UnknownBlock unknownBlock = (UnknownBlock) block;
            com.itextpdf.text.Paragraph paragraph = new com.itextpdf.text.Paragraph();
            paragraph.setFont(unknown);
            paragraph.add("Unexpected ");
            paragraph.add(unknownBlock.getName());
            paragraph.add(" found at ");
            paragraph.add(unknownBlock.getLocation().getFile());
            paragraph.add(", line: ");
            paragraph.add(String.valueOf(unknownBlock.getLocation().getLine()));
            paragraph.add(", column: ");
            paragraph.add(String.valueOf(unknownBlock.getLocation().getColumn()));
            return paragraph;
        } else {
            throw new IllegalStateException(String.format("Don't know how to render block of type '%s'.",
                    block.getClass().getSimpleName()));
        }
    }

    private void addItems(List list, com.itextpdf.text.List pdfList) throws DocumentException {
        for (ListItem item : list.getItems()) {
            com.itextpdf.text.ListItem pdfItem = new com.itextpdf.text.ListItem();
            pdfList.add(pdfItem);

            // TODO - theme spacing
            pdfItem.setSpacingBefore(2);
            pdfItem.setSpacingAfter(2);
            for (Block childBlock : item.getContents()) {
                pdfItem.add(convertBlock(childBlock));
            }
            pdfItem.getListSymbol().setFont(base);
        }
    }
}