package net.rubygrapefruit.docs.model;

public class BuildableCrossReference implements CrossReference, BuildableInline {
    private LinkTarget target;
    private LinkResolver resolver;

    public BuildableCrossReference(LinkResolver resolver) {
        this.resolver = resolver;
    }

    public LinkTarget getTarget() {
        return target;
    }

    public void finish() {
        try {
            target = resolver.resolve();
        } finally {
            resolver = null;
        }
    }

    public String getText() {
        return "";
    }
}
