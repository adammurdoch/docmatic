package net.rubygrapefruit.docs.pdf;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Font;
import net.rubygrapefruit.docs.theme.TextTheme;

public class FontStack {
    private final Font base;
    private final TextTheme theme;

    public FontStack(TextTheme textTheme) {
        Font.FontFamily fontFamily = Font.FontFamily.TIMES_ROMAN;
        BaseColor textColor = BaseColor.BLACK;
        if (textTheme != null) {
            fontFamily = toFont(textTheme.getFontName());
            textColor = new BaseColor(textTheme.getColour());
        }

        // TODO - theme font sizes
        base = new Font(fontFamily, 10, Font.NORMAL, textColor);
        theme = textTheme;
    }

    private Font.FontFamily toFont(String fontName) {
        Font.FontFamily fontFamily;
        fontFamily = fontName.equals("sans-serif") ? Font.FontFamily.HELVETICA
                : Font.FontFamily.TIMES_ROMAN;
        return fontFamily;
    }

    private FontStack(Font base, TextTheme theme) {
        this.base = base;
        this.theme = theme;
    }

    private FontStack withBase(Font font) {
        return new FontStack(font, theme);
    }

    public FontStack getHeader(int depth) {
        Font.FontFamily fontFamily = base.getFamily();
        BaseColor textColor = base.getColor();
        if (theme != null) {
            fontFamily = toFont(theme.getHeaderFontName());
            textColor = new BaseColor(theme.getHeaderColour());
        }

        switch (depth) {
            case 0:
                return withBase(new Font(fontFamily, 22, Font.BOLD, textColor));
            case 1:
                return withBase(new Font(fontFamily, 14, Font.BOLD, textColor));
            case 2:
                return withBase(new Font(fontFamily, 12, Font.BOLD, textColor));
            default:
                return withBase(new Font(fontFamily, 10, Font.BOLD, textColor));
        }
    }

    public Font getBase() {
        return base;
    }

    public Font getError() {
        return new Font(base.getFamily(), base.getSize(), base.getStyle(), BaseColor.RED);
    }

    public FontStack getMonospaced() {
        return withBase(new Font(Font.FontFamily.COURIER, base.getSize(), base.getStyle(), base.getColor()));
    }

    public FontStack getItalic() {
        return withBase(new Font(base.getFamily(), base.getSize(), base.getStyle() | Font.ITALIC, base.getColor()));
    }

    public FontStack getUnderline() {
        return withBase(new Font(base.getFamily(), base.getSize(), base.getStyle() | Font.UNDERLINE, base.getColor()));
    }

    public FontStack getBold() {
        return withBase(new Font(base.getFamily(), base.getSize(), base.getStyle() | Font.BOLD, base.getColor()));
    }
}
