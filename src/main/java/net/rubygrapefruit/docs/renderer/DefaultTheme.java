package net.rubygrapefruit.docs.renderer;

import java.awt.*;

public class DefaultTheme implements Theme, TextTheme {
    public TextTheme asTextTheme() {
        return this;
    }

    public String getFontName() {
        return "sans-serif";
    }

    public Color getColour() {
//        return Color.BLUE;
        return new Color(80, 80, 80);
    }
}
