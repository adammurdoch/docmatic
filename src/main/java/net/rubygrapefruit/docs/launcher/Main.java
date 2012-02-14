package net.rubygrapefruit.docs.launcher;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.rubygrapefruit.docs.docbook.DocbookParser;
import net.rubygrapefruit.docs.html.HtmlRenderer;
import net.rubygrapefruit.docs.markdown.MarkdownParser;
import net.rubygrapefruit.docs.model.Document;
import net.rubygrapefruit.docs.parser.Parser;
import net.rubygrapefruit.docs.pdf.PdfRenderer;
import net.rubygrapefruit.docs.renderer.Renderer;
import net.rubygrapefruit.docs.theme.DefaultTheme;
import net.rubygrapefruit.docs.theme.FixedWidthTheme;
import net.rubygrapefruit.docs.theme.MinimalTheme;
import net.rubygrapefruit.docs.theme.Theme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        OptionParser optionParser = new OptionParser();
        optionParser.accepts("html", "Generate HTML 4 output");
        optionParser.accepts("pdf", "Generate PDF output");
        optionParser.accepts("minimal", "Use the minimal theme");
        optionParser.accepts("default", "Use the default theme");
        optionParser.accepts("fixed-width", "Use the fixed width theme (HTML only)");
        optionParser.accepts("out", "The directory to generate output to").withRequiredArg().required();

        OptionSet result = null;
        try {
            result = optionParser.parse(args);
        } catch (OptionException e) {
            System.err.println(e.getMessage());
            System.err.println();
            System.err.println("USAGE: docmatic [options] --out <output-dir> <input-file>...");
            System.err.println();
            optionParser.printHelpOn(System.err);
            System.exit(1);
        }

        List<File> inputs = new ArrayList<File>();
        for (String s : result.nonOptionArguments()) {
            inputs.add(new File(s));
        }

        boolean html = result.has("html");
        boolean pdf = result.has("pdf");

        if (!html && !pdf) {
            LOGGER.warn("No output formats specified. Generating HTML.");
            html = true;
        }

        boolean minimalOut = result.has("minimal");
        boolean defaultOut = result.has("default");
        boolean fixedWidthOut = result.has("fixed-width");

        if (!minimalOut && !defaultOut && !fixedWidthOut) {
            defaultOut = true;
        }

        File outputDir = new File(result.valueOf("out").toString());

        Renderer htmlRenderer = new HtmlRenderer();
        Renderer pdfRenderer = new PdfRenderer();
        Theme minimalTheme = new MinimalTheme();
        Theme defaultTheme = new DefaultTheme();
        Theme fixedWidthTheme = new FixedWidthTheme();
        for (File input : inputs) {
            Parser parser;
            if (input.getName().endsWith(".xml")) {
                parser = new DocbookParser();
            } else {
                parser = new MarkdownParser();
            }
            Document document = parser.parse(input);

            if (html) {
                if (minimalOut) {
                    File htmlOutput = new File(outputDir, input.getName() + ".minimal.html");
                    htmlRenderer.render(document, minimalTheme, htmlOutput);
                }
                if (fixedWidthOut) {
                    File htmlOutput = new File(outputDir, input.getName() + ".fixed.html");
                    htmlRenderer.render(document, fixedWidthTheme, htmlOutput);
                }
                if (defaultOut) {
                    File htmlOutput = new File(outputDir, input.getName() + ".html");
                    htmlRenderer.render(document, defaultTheme, htmlOutput);
                }
            }

            if (pdf) {
                if (minimalOut) {
                    File pdfOutput = new File(outputDir, input.getName() + ".minimal.pdf");
                    pdfRenderer.render(document, minimalTheme, pdfOutput);
                }
                if (defaultOut) {
                    File pdfOutput = new File(outputDir, input.getName() + ".pdf");
                    pdfRenderer.render(document, defaultTheme, pdfOutput);
                }
            }
        }
    }
}
