package net.rubygrapefruit.docs.model.buildable;

import net.rubygrapefruit.docs.model.Referenceable;

import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

public class UnresolvedLink extends BuildableInlineContainer implements BuildableInline {
    private LinkResolver resolver;

    public UnresolvedLink(LinkResolver resolver) {
        this.resolver = resolver;
    }

    public BuildableInline resolve() {
        final AtomicReference<BuildableInline> result = new AtomicReference<BuildableInline>();
        resolver.resolve(new LinkResolverContext() {
            public void error(String message) {
                result.set(new BuildableErrorElement(message));
            }

            public void crossReference(Referenceable target) {
                BuildableCrossReference crossReference = new BuildableCrossReference(target);
                moveContentsTo(crossReference);
                result.set(crossReference);
            }

            public void url(URI target) {
                BuildableLink link = new BuildableLink(target);
                moveContentsTo(link);
                result.set(link);
            }
        });
        return result.get();
    }
}
