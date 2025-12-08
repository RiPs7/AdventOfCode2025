package com.rips7.util.algorithms.pathfinding;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import java.util.function.Function;

import static com.rips7.util.algorithms.pathfinding.Node.backtrack;
import static com.rips7.util.algorithms.pathfinding.Node.node;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class DFS<T> {

    public List<T> run(final T start, final Function<T, List<T>> neighborsGetter) {
        return run(start, null, neighborsGetter, false);
    }

    public List<T> run(final T start, final T end, final Function<T, List<T>> neighborsGetter) {
        return run(start, end, neighborsGetter, false);
    }

    public List<T> run(final T start, final T end, final Function<T, List<T>> neighborsGetter, final boolean isStartSameAsEnd) {
        final Stack<Node<T>> frontier = new Stack<>();
        final Set<Node<T>> closed = new HashSet<>();

        final Node<T> startNode = node(start);
        final Node<T> endNode = end == null ? null : node(end);

        if (isStartSameAsEnd) {
            neighborsGetter.apply(start).stream()
                    .map(Node::node)
                    .peek(n -> n.parent(startNode))
                    .forEach(frontier::add);
        } else {
            frontier.push(startNode);
        }
        while (!frontier.isEmpty()) {
            final Node<T> current = frontier.pop();
            if (current.equals(endNode)) {
                return backtrack(current);
            }
            if (closed.contains(current)) {
                continue;
            }
            neighborsGetter.apply(current.data()).stream()
                    .map(Node::node)
                    .filter(n -> !Objects.equals(n, current.parent()))
                    .peek(n -> n.parent(current))
                    .forEach(frontier::add);
            closed.add(current);
        }
        if (endNode != null) {
            throw new RuntimeException("No solution found");
        }
        return Collections.emptyList();
    }
}
