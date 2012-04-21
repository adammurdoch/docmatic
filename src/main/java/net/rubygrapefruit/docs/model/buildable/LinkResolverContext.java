package net.rubygrapefruit.docs.model.buildable;

import net.rubygrapefruit.docs.model.Referenceable;

import java.net.URI;

public interface LinkResolverContext {
    void error(String message);

    void crossReference(Referenceable target);

    void url(URI target);
}
