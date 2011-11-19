package net.rubygrapefruit.docs.renderer;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import net.rubygrapefruit.docs.model.Document;
import net.rubygrapefruit.docs.model.Paragraph;

import java.io.OutputStream;

public class PdfRenderer extends Renderer {
    @Override
    public void render(Document document, OutputStream stream) {
        try {
            com.itextpdf.text.Document pdfDocument = new com.itextpdf.text.Document(PageSize.A4);
            PdfWriter.getInstance(pdfDocument, stream);
            pdfDocument.open();
            for (Paragraph paragraph : document.getContents(Paragraph.class)) {
                pdfDocument.add(new com.itextpdf.text.Paragraph(paragraph.getText()));
            }
            pdfDocument.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Could not generate PDF for document.", e);
        }
    }
}
