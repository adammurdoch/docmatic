package net.rubygrapefruit.docs.html;

import net.rubygrapefruit.docs.theme.Theme;

import java.io.IOException;

public interface HtmlTheme extends Theme {
    void writeStyleRules(Appendable target) throws IOException;
}
