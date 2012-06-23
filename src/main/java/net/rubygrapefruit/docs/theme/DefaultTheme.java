package net.rubygrapefruit.docs.theme;

import java.awt.*;
import java.math.BigDecimal;

public class DefaultTheme implements Theme, TextTheme {
    public RenderableDocumentBuilder getDocumentBuilder() {
        return new MultipleChunkBuilder();
    }

    /**
     * Default implementation simply checks if this theme object implements the given type, and returns 'this' if so.
     */
    public <T extends Aspect> T getAspect(Class<T> type) {
        if (type.isInstance(this)) {
            return type.cast(this);
        }
        return null;
    }

    public String getFontName() {
        return "sans-serif";
    }

    public Color getColour() {
//        return Color.BLUE;
        return new Color(60, 60, 60);
    }

    public String getHeaderFontName() {
        return getFontName();
    }

    public Color getHeaderColour() {
        return getColour();
    }

    public BigDecimal getLineSpacing() {
        return BigDecimal.valueOf(14, 1);
    }
}
