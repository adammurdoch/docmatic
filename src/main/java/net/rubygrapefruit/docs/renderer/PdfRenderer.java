package net.rubygrapefruit.docs.renderer;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import net.rubygrapefruit.docs.model.Block;
import net.rubygrapefruit.docs.model.Document;
import net.rubygrapefruit.docs.model.Paragraph;
import net.rubygrapefruit.docs.model.Section;

import java.io.OutputStream;

public class PdfRenderer extends Renderer {
    private final Font base = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.BLACK);
    private final Font h1 = new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD, BaseColor.BLACK);
    private final Font h2 = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, BaseColor.BLACK);

    @Override
    public void render(Document document, OutputStream stream) {
        try {
            com.itextpdf.text.Document pdfDocument = new com.itextpdf.text.Document(PageSize.A4);
            PdfWriter.getInstance(pdfDocument, stream);
            pdfDocument.open();
            writeContents(document, 0, pdfDocument);
            pdfDocument.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Could not generate PDF for document.", e);
        }
    }

    private void writeContents(Section section, int depth, com.itextpdf.text.Document target)
            throws DocumentException {
        for (Block block : section.getContents()) {
            if (block instanceof Section) {
                Section child = (Section) block;
                target.add(new com.itextpdf.text.Paragraph(child.getTitle(), depth == 0 ? h1 : h2));
                writeContents(child, depth+1, target);
            } else {
                Paragraph paragraph = (Paragraph) block;
                target.add(new com.itextpdf.text.Paragraph(paragraph.getText(), base));
            }
        }
    }
}
