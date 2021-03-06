package net.rubygrapefruit.docs.theme;

import java.awt.*;
import java.math.BigDecimal;
import java.math.BigInteger;

public interface TextTheme extends Aspect {
    public String getFontName();

    public Color getColour();

    public String getHeaderFontName();

    public Color getHeaderColour();

    /**
     * Returns line spacing, in points.
     */
    public BigDecimal getLineSpacing();
}
