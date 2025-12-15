package com.rips7.util.algorithms.graphs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Graph<T> {
    private final Map<T, Node<T>> lookup;

    public Graph(final Set<T> nodes, final Map<T, Set<T>> connections) {
        lookup = nodes.stream().collect(Collectors.toMap(
                Function.identity(),
                Node::new));
        connections.forEach((key, value) -> {
            final Node<T> node = lookup.get(key);
            for (final T neighbor : value) {
                node.neighbors.add(lookup.get(neighbor));
            }
        });
    }

    public long countPaths(final T start, final T end) {
        final Map<Node<T>, Long> memo = new HashMap<>();
        return countPaths(lookup.get(start), lookup.get(end), memo);
    }

    private long countPaths(final Node<T> current, final Node<T> end, final Map<Node<T>, Long> memo) {
        if (current.equals(end)) {
            return 1;
        }
        if (memo.containsKey(current)) {
            return memo.get(current);
        }

        long paths = 0;
        for (Node<T> neighbor : current.neighbors) {
            paths += countPaths(neighbor, end, memo);
        }
        memo.put(current, paths);
        return paths;
    }

    private static final class Node<T> {
        final T data;
        final Set<Node<T>> neighbors = new HashSet<>();

        Node(T data) {
            this.data = data;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Node<?> node)) {
                return false;
            }
            return data.equals(node.data);
        }

        @Override
        public int hashCode() {
            return data.hashCode();
        }
    }

}
