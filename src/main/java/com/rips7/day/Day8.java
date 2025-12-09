package com.rips7.day;

import com.rips7.util.Util.Pair;
import com.rips7.util.algorithms.graphs.DisjointSetUnion;
import com.rips7.util.maths.Maths.Vector3D;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.IntStream;

public class Day8 implements Day<Long> {

    private static final int CONNECTIONS = 1000;

    @Override
    public Long part1(String input) {
        final List<Vector3D<Long>> boxes = parseBoxes(input);

        final List<Set<Vector3D<Long>>> circuits = connectCircuits(boxes);

        return circuits.stream()
                .limit(3)
                .mapToLong(Set::size)
                .reduce(1L, (a, b) -> a * b);
    }

    @Override
    public Long part2(String input) {
        final List<Vector3D<Long>> boxes = parseBoxes(input);

        final Pair<Vector3D<Long>, Vector3D<Long>> lastTwoBoxes = connectSingleCircuit(boxes);

        return lastTwoBoxes.left().x() * lastTwoBoxes.right().x();
    }

    private List<Vector3D<Long>> parseBoxes(final String input) {
        return input.lines()
                .map(line -> line.split(","))
                .map(parts -> Vector3D.of(
                        Long.parseLong(parts[0]),
                        Long.parseLong(parts[1]),
                        Long.parseLong(parts[2])))
                .toList();
    }

    private List<Set<Vector3D<Long>>> connectCircuits(final List<Vector3D<Long>> boxes) {
        final DisjointSetUnion<Vector3D<Long>> dsu = new DisjointSetUnion<>(boxes);

        final List<Edge> edges = getEdges(boxes);

        for (int i = 0; i < CONNECTIONS && i < edges.size(); i++) {
            final Edge edge = edges.get(i);
            dsu.union(edge.u(), edge.v());
        }

        return dsu.getSets().stream()
                .sorted(Comparator.<Set<Vector3D<Long>>>comparingInt(Set::size).reversed())
                .toList();
    }

    private Pair<Vector3D<Long>, Vector3D<Long>> connectSingleCircuit(final List<Vector3D<Long>> boxes) {
        final DisjointSetUnion<Vector3D<Long>> dsu = new DisjointSetUnion<>(boxes);

        final List<Edge> edges = getEdges(boxes);

        int i = 0;
        Edge edge;
        do {
            edge = edges.get(i++);
            dsu.union(edge.u(), edge.v());
        } while(!dsu.isFullyConnected());

        return Pair.of(edge.u(), edge.v());
    }

    private List<Edge> getEdges(final List<Vector3D<Long>> boxes) {
        return IntStream.range(0, boxes.size() - 1)
                .mapToObj(i -> IntStream.range(i + 1, boxes.size())
                        .mapToObj(j -> new Edge(boxes.get(i), boxes.get(j), distSq(boxes.get(i), boxes.get(j)))))
                .flatMap(Function.identity())
                .sorted(Comparator.comparingLong(Edge::distanceSq))
                .toList();
    }

    private long distSq(final Vector3D<Long> box1, final Vector3D<Long> box2) {
        final long distX = box1.x() - box2.x();
        final long distY = box1.y() - box2.y();
        final long distZ = box1.z() - box2.z();
        return distX * distX + distY * distY + distZ * distZ;
    }

    private record Edge(Vector3D<Long> u, Vector3D<Long> v, long distanceSq) {}
}
