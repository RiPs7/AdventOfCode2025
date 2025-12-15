package com.rips7.util.algorithms.pathfinding;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.rips7.util.algorithms.pathfinding.Node.backtrack;
import static com.rips7.util.algorithms.pathfinding.Node.node;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class BFS<T> {

    public List<T> run(final T start, final Function<T, List<T>> neighborsGetter) {
        return run(start, null, (n) -> false, neighborsGetter, false);
    }

    public List<T> run(final T start, final T end, final Function<T, List<T>> neighborsGetter) {
        return run(start, end, (n) -> false, neighborsGetter);
    }

    public List<T> run(final T start, final T end, final Predicate<T> pruner, final Function<T, List<T>> neighborsGetter) {
        return run(start, end, (n) -> false, neighborsGetter, false);
    }

    public List<T> run(final T start, final T end, final Predicate<T> pruner, final Function<T, List<T>> neighborsGetter, final boolean isStartSameAsEnd) {
        final Queue<Node<T>> frontier = new ArrayDeque<>();
        final Set<Node<T>> closed = new HashSet<>();

        final Node<T> startNode = node(start);
        final Node<T> endNode = end == null ? null : node(end);

        if (isStartSameAsEnd) {
            neighborsGetter.apply(start).stream()
                    .map(Node::node)
                    .peek(n -> n.parent(startNode))
                    .forEach(frontier::add);
        } else {
            frontier.add(startNode);
        }
        while (!frontier.isEmpty()) {
            final Node<T> current = frontier.poll();
            if (current.equals(endNode)) {
                return backtrack(current);
            }
            if (closed.contains(current)) {
                continue;
            }
            if (pruner.test(current.data())) {
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
