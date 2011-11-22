package net.rubygrapefruit.docs.pdf;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import net.rubygrapefruit.docs.model.*;
import net.rubygrapefruit.docs.model.Document;
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
        base = new Font(fontFamily, 12, Font.NORMAL, textColor);
        h1 = new Font(fontFamily, 22, Font.BOLD, textColor);
        h2 = new Font(fontFamily, 16, Font.BOLD, textColor);
        unknown = new Font(fontFamily, 12, Font.NORMAL, BaseColor.RED);

        com.itextpdf.text.Document pdfDocument = new com.itextpdf.text.Document(PageSize.A4);
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
                target.add(new com.itextpdf.text.Paragraph(child.getTitle(), depth == 0 ? h1 : h2));
                writeContents(child, depth + 1, target);
            } else {
                target.add(convertBlock(block));
            }
        }
    }

    private Element convertBlock(Block block) throws DocumentException {
        if (block instanceof Paragraph) {
            Paragraph paragraph = (Paragraph) block;
            return new com.itextpdf.text.Paragraph(paragraph.getText(), base);
        } else if (block instanceof ItemisedList) {
            ItemisedList list = (ItemisedList) block;
            List pdfList = new List(false);
            pdfList.setListSymbol("\u2022   ");
            for (ListItem item : list.getItems()) {
                com.itextpdf.text.ListItem pdfItem = new com.itextpdf.text.ListItem();
                for (Block childBlock : item.getContents()) {
                    pdfItem.add(convertBlock(childBlock));
                }
                pdfList.add(pdfItem);
            }
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
}
