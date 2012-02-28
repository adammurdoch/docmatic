package net.rubygrapefruit.docs.model.buildable;

import net.rubygrapefruit.docs.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class BuildableInlineContainer implements InlineContainer {
    private final List<BuildableInline> contents = new ArrayList<BuildableInline>();
    private BuildableText text;
    private boolean needWhitespace;

    public void finish() {
        ListIterator<BuildableInline> iter = contents.listIterator();
        while (iter.hasNext()) {
            BuildableInline inline = iter.next();
            if (inline instanceof UnresolvedLink) {
                iter.remove();
                iter.add(((UnresolvedLink) inline).resolve());
            } else {
                inline.finish();
            }
        }
    }

    public String getText() {
        StringBuilder builder = new StringBuilder();
        for (Inline content : contents) {
            builder.append(content.getText());
        }
        return builder.toString();
    }

    public boolean isEmpty() {
        return contents.isEmpty();
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

    protected <T extends BuildableInline> T add(T element) {
        contents.add(element);
        if (text != null && needWhitespace) {
            text.append(' ');
            needWhitespace = false;
        }
        text = null;
        return element;
    }

    public net.rubygrapefruit.docs.model.Error addError(String message) {
        return add(new BuildableErrorElement(message));
    }

    public List<? extends Inline> getContents() {
        return contents;
    }

    public BuildableCode addCode() {
        return add(new BuildableCode());
    }

    public BuildableLiteral addLiteral() {
        return add(new BuildableLiteral());
    }

    public BuildableEmphasis addEmphasis() {
        return add(new BuildableEmphasis());
    }

    public void addCrossReference(LinkResolver resolver) {
        add(new UnresolvedLink(resolver));
    }
}
