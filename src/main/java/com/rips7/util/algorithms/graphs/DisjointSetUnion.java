package com.rips7.util.algorithms.graphs;


import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DisjointSetUnion<T> {
    private final List<T> elements;
    private final Map<T, T> parent = new HashMap<>();
    private final Map<T, Integer> size = new HashMap<>();

    public DisjointSetUnion(final List<T> elements) {
        this.elements = elements;
        for (final T element : elements) {
            parent.put(element, element);
            size.put(element, 1);
        }
    }

    public T find(final T element) {
        if (parent.get(element).equals(element)) {
            return element;
        }
        final T root = find(parent.get(element));
        parent.put(element, root);
        return root;
    }

    public void union(final T element1, final T element2) {
        T root1 = find(element1);
        T root2 = find(element2);

        if (root1.equals(root2)) {
            return;
        }

        if (size.get(root1) < size.get(root2)) {
            T temp = root1;
            root1 = root2;
            root2 = temp;
        }

        parent.put(root2, root1);
        size.put(root1, size.get(root1) + size.get(root2));
        size.remove(root2);
    }

    public List<Set<T>> getSets() {
        final Map<T, Set<T>> sets = new HashMap<>();
        for (final T element : this.elements) {
            final T root = find(element);
            sets.computeIfAbsent(root, k -> new HashSet<>()).add(element);
        }
        return sets.values().stream().toList();
    }

    public boolean isFullyConnected() {
        return size.size() == 1;
    }
}