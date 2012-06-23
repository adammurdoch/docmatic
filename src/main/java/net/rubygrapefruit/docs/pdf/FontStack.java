package net.rubygrapefruit.docs.pdf;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Font;
import net.rubygrapefruit.docs.theme.TextTheme;

public class FontStack {
    private final Font base;

    public FontStack(TextTheme textTheme) {
        Font.FontFamily fontFamily = Font.FontFamily.TIMES_ROMAN;
        BaseColor textColor = BaseColor.BLACK;
        if (textTheme != null) {
            fontFamily = textTheme.getFontName().equals("sans-serif") ? Font.FontFamily.HELVETICA
                    : Font.FontFamily.TIMES_ROMAN;
            textColor = new BaseColor(textTheme.getColour());
        }

        // TODO - theme font sizes
        base = new Font(fontFamily, 12, Font.NORMAL, textColor);
    }

    private FontStack(Font base) {
        this.base = base;
    }

    public FontStack getHeader(int depth) {
        switch (depth) {
            case 0:
                return new FontStack(new Font(base.getFamily(), 22, Font.BOLD, base.getColor()));
            case 1:
                return new FontStack(new Font(base.getFamily(), 16, Font.BOLD, base.getColor()));
            case 2:
                return new FontStack(new Font(base.getFamily(), 14, Font.BOLD, base.getColor()));
            default:
                return new FontStack(new Font(base.getFamily(), 12, Font.BOLD, base.getColor()));
        }
    }

    public Font getBase() {
        return base;
    }

    public Font getError() {
        return new Font(base.getFamily(), base.getSize(), base.getStyle(), BaseColor.RED);
    }

    public FontStack getMonospaced() {
        return new FontStack(new Font(Font.FontFamily.COURIER, base.getSize(), base.getStyle(), base.getColor()));
    }

    public FontStack getItalic() {
        return new FontStack(new Font(base.getFamily(), base.getSize(), base.getStyle() | Font.ITALIC, base.getColor()));
    }

    public FontStack getUnderline() {
        return new FontStack(new Font(base.getFamily(), base.getSize(), base.getStyle() | Font.UNDERLINE, base.getColor()));
    }
}
