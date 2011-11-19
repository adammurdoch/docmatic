package net.rubygrapefruit.docs.launcher;

import net.rubygrapefruit.docs.docbook.DocbookParser;
import net.rubygrapefruit.docs.markdown.MarkdownParser;
import net.rubygrapefruit.docs.model.Document;
import net.rubygrapefruit.docs.parser.Parser;
import net.rubygrapefruit.docs.renderer.HtmlRenderer;
import net.rubygrapefruit.docs.renderer.PdfRenderer;
import net.rubygrapefruit.docs.renderer.Renderer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<File> inputs = new ArrayList<File>();
        for (int i = 0; i < args.length - 1; i++) {
            inputs.add(new File(args[i]));
        }
        File outputDir = new File(args[args.length - 1]);

        Renderer htmlRenderer = new HtmlRenderer();
        Renderer pdfRenderer = new PdfRenderer();
        for (File input : inputs) {
            Parser parser;
            if (input.getName().endsWith(".xml")) {
                parser = new DocbookParser();
            } else {
                parser = new MarkdownParser();
            }
            Document document = parser.parse(input);

            File htmlOutput = new File(outputDir, input.getName() + ".html");
            htmlRenderer.render(document, htmlOutput);

            File pdfOutput = new File(outputDir, input.getName() + ".pdf");
            pdfRenderer.render(document, pdfOutput);
        }
    }
}
