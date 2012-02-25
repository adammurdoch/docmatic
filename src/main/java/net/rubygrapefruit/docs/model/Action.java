package net.rubygrapefruit.docs.model;

public interface Action<T> {
    void execute(T object);
}
