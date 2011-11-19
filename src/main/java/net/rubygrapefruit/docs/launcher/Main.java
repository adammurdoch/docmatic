package net.rubygrapefruit.docs.launcher;

import net.rubygrapefruit.docs.docbook.DocbookParser;
import net.rubygrapefruit.docs.model.Document;
import net.rubygrapefruit.docs.markdown.MarkdownParser;
import net.rubygrapefruit.docs.parser.Parser;
import net.rubygrapefruit.docs.renderer.HtmlRenderer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<File> inputs = new ArrayList<File>();
        for (int i = 0; i < args.length -1; i++) {
            inputs.add(new File(args[i]));
        }
        File outputDir = new File(args[args.length - 1]);

        HtmlRenderer renderer = new HtmlRenderer();
        for (File input : inputs) {
            Parser parser;
            if (input.getName().endsWith(".xml")) {
                parser = new DocbookParser();
            } else {
                parser = new MarkdownParser();
            }
            File output = new File(outputDir, input.getName() + ".html");
            System.out.format("%s -> %s%n", input, output);
            Document document = parser.parse(input);
            renderer.render(document, output);
        }
    }
}
