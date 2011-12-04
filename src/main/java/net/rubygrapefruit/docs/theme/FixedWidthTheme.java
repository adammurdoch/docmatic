package net.rubygrapefruit.docs.theme;

import net.rubygrapefruit.docs.html.HtmlTheme;

import java.io.IOException;

public class FixedWidthTheme extends DefaultTheme implements HtmlTheme {
    public void writeStyleRules(Appendable target) throws IOException {
        target.append("html { background-color: #E9E5e2; }\n");
        target.append(
                "body { width: 780px; background-color: white; margin-left: auto; margin-right: auto; padding-left: 2em; padding-right: 2em; padding-top: 1em; padding-bottom: 1em; border: solid #d0d0d0 1px; }\n");
        target.append("h1,h2, h3, h4, h5, h6 { color: #2B5366 }\n");
    }
}
