package com.rips7.util.algorithms.pathfinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public final class Node<T> {
    private final T data;
    private final AtomicReference<Node<T>> parent;

    public static <T> Node<T> node(final T data) {
        return new Node<>(data);
    }

    public static <T> List<T> backtrack(final Node<T> end) {
        final List<Node<T>> path = new ArrayList<>();
        Node<T> current = end;
        while (current != null) {
            path.add(current);
            current = current.parent();
        }
        Collections.reverse(path);
        return path.stream().map(Node::data).toList();
    }

    public Node(final T data, final AtomicReference<Node<T>> parent) {
        this.data = data;
        this.parent = parent;
    }

    public Node(final T data) {
        this(data, new AtomicReference<>());
    }

    public T data() {
        return data;
    }

    public Node<T> parent() {
        return parent.get();
    }

    public void parent(final Node<T> parent) {
        this.parent.set(parent);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(data);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Node<?> other)) return false;
        return Objects.equals(this.data, other.data);
    }

    @Override
    public String toString() {
        return "[%s]".formatted(data);
    }
}