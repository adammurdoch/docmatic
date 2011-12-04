package net.rubygrapefruit.docs.theme;

import net.rubygrapefruit.docs.renderer.TextTheme;

import java.awt.*;
import java.math.BigDecimal;

public class DefaultTheme implements Theme, TextTheme {
    public TextTheme asTextTheme() {
        return this;
    }

    public String getFontName() {
        return "sans-serif";
    }

    public Color getColour() {
//        return Color.BLUE;
        return new Color(60, 60, 60);
    }

    public BigDecimal getLineSpacing() {
        return BigDecimal.valueOf(14, 1);
    }
}
