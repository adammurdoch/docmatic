package net.rubygrapefruit.docs.renderer;

import java.awt.*;
import java.math.BigDecimal;
import java.math.BigInteger;

public interface TextTheme {
    public String getFontName();

    public Color getColour();

    /**
     * Returns line spacing, in points.
     */
    public BigDecimal getLineSpacing();
}
