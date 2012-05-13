package net.rubygrapefruit.docs.model.buildable;

import net.rubygrapefruit.docs.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class BuildableInlineContainer implements InlineContainer {
    private final List<BuildableInline> contents = new ArrayList<BuildableInline>();
    private BuildableText text;
    private boolean needWhitespace;

    @Override
    public String toString() {
        return String.format("[%s text:%s]", getTypeName(), getText());
    }

    protected String getTypeName() {
        return getClass().getSimpleName().replaceFirst("^Buildable", "").toLowerCase();
    }

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
        for (Inline content : getContents()) {
            builder.append(content.getText());
        }
        return builder.toString();
    }

    public boolean isEmpty() {
        return contents.isEmpty();
    }

    public void moveContentsTo(BuildableInlineContainer target) {
        for (BuildableInline element : contents) {
            target.add(element);
        }
        contents.clear();
    }

    public BuildableInlineContainer append(CharSequence src) {
        boolean isFirstElement = contents.isEmpty();
        int pos = 0;
        while (pos < src.length()) {
            char ch = src.charAt(pos);
            if (!Character.isWhitespace(ch)) {
                if (text == null) {
                    text = new BuildableText();
                    contents.add(text);
                }
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
        if (isFirstElement && text == null) {
            needWhitespace = false;
        }
        return this;
    }

    protected <T extends BuildableInline> T add(T element) {
        if (needWhitespace) {
            if (text == null) {
                text = new BuildableText();
                contents.add(text);
            }
            text.append(' ');
            needWhitespace = false;
        }
        text = null;
        contents.add(element);
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

    public BuildableInlineContainer addCrossReference(LinkResolver resolver) {
        return add(new UnresolvedLink(resolver));
    }
}
