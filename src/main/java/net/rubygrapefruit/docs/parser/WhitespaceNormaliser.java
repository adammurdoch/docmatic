package net.rubygrapefruit.docs.parser;

public class WhitespaceNormaliser {
    private final StringBuilder text = new StringBuilder();
    boolean needWhitespace;
    
    public CharSequence getText() {
        return text;
    }

    public void append(CharSequence source) {
        int pos = 0;
        while (pos < source.length()) {
            char ch = source.charAt(pos);
            if (!Character.isWhitespace(ch)) {
                if (needWhitespace) {
                    text.append(' ');
                    needWhitespace = false;
                }
                text.append(ch);
                pos++;
                continue;
            }
            int end = pos + 1;
            while (end < source.length() && Character.isWhitespace(source.charAt(end))) {
                end++;
            }
            if (text.length() > 0) {
                needWhitespace = true;
            }
            pos = end;
        }
    }
}
