package net.rubygrapefruit.docs.model.buildable;

import net.rubygrapefruit.docs.model.Referenceable;

public interface LinkResolverContext {
    void error(String message);

    void crossReference(Referenceable target);
}
