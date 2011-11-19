package net.rubygrapefruit.docs.renderer;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import net.rubygrapefruit.docs.model.*;
import net.rubygrapefruit.docs.model.Document;
import net.rubygrapefruit.docs.model.Paragraph;
import net.rubygrapefruit.docs.model.Section;

import java.io.OutputStream;

public class PdfRenderer extends Renderer {
    private final Font base = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.BLACK);
    private final Font h1 = new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD, BaseColor.BLACK);
    private final Font h2 = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, BaseColor.BLACK);
    private final Font unknown = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.RED);

    @Override
    protected void doRender(Document document, OutputStream stream) throws Exception {
        com.itextpdf.text.Document pdfDocument = new com.itextpdf.text.Document(PageSize.A4);
        PdfWriter.getInstance(pdfDocument, stream);
        pdfDocument.open();
        writeContents(document, 0, pdfDocument);
        pdfDocument.close();
    }

    private void writeContents(Section section, int depth, com.itextpdf.text.Document target) throws DocumentException {
        for (Block block : section.getContents()) {
            if (block instanceof Section) {
                Section child = (Section) block;
                target.add(new com.itextpdf.text.Paragraph(child.getTitle(), depth == 0 ? h1 : h2));
                writeContents(child, depth + 1, target);
            } else if (block instanceof Paragraph) {
                Paragraph paragraph = (Paragraph) block;
                target.add(new com.itextpdf.text.Paragraph(paragraph.getText(), base));
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
                target.add(paragraph);
            } else {
                throw new IllegalStateException(String.format("Don't know how to render block of type '%s'.",
                        block.getClass().getSimpleName()));
            }
        }
    }
}
