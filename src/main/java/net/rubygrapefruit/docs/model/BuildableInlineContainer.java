package net.rubygrapefruit.docs.model;

import java.util.ArrayList;
import java.util.List;

public class BuildableInlineContainer implements InlineContainer {
    private final List<Inline> contents = new ArrayList<Inline>();
    private BuildableText text;
    private boolean needWhitespace;

    public String getText() {
        StringBuilder builder = new StringBuilder();
        for (Inline content : contents) {
            builder.append(content.getText());
        }
        return builder.toString();
    }

    public void append(CharSequence src) {
        boolean isFirstElement = contents.isEmpty();
        if (text == null) {
            text = add(new BuildableText());
        }
        int pos = 0;
        while (pos < src.length()) {
            char ch = src.charAt(pos);
            if (!Character.isWhitespace(ch)) {
                if (needWhitespace) {
                    text.append(' ');
                    needWhitespace = false;
                }
                text.append(ch);
                pos++;
            } else {
                int end = pos + 1;
                while (end < src.length() && Character.isWhitespace(src.charAt(end))) {
                    end++;
                }
                if (!isFirstElement || pos > 0) {
                    needWhitespace = true;
                }
                pos = end;
            }
        }
    }

    protected <T extends Inline> T add(T element) {
        contents.add(element);
        if (text != null && needWhitespace) {
            text.append(' ');
            needWhitespace = false;
        }
        text = null;
        return element;
    }

    public Unknown addUnknown(String message) {
        return add(new DefaultUnknown(message));
    }

    public List<? extends Inline> getContents() {
        return contents;
    }
}
