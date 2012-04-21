package net.rubygrapefruit.docs.model;

public interface CrossReference extends Span {
    Referenceable getTarget();
}
