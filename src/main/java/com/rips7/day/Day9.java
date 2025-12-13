package com.rips7.day;

import com.rips7.util.Util;
import com.rips7.util.Util.Grid;
import com.rips7.util.Util.Pair;
import com.rips7.util.Util.Position;
import com.rips7.util.algorithms.dynamic.PSA;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Day9 implements Day<Long> {

    @Override
    public Long part1(String input) {
        final Loop loop = Loop.parse(input);

        return loop.pairPoints()
                .map(pair -> rectangleArea(pair.left(), pair.right()))
                .mapToLong(Long::longValue)
                .max()
                .orElse(0);
    }

    @Override
    public Long part2(String input) {
        final Loop loop = Loop.parse(input);

        final Grid<Integer> compressedGrid = compressGrid(loop);
        floodFill(compressedGrid);

        final PSA<Integer[][]> gridPSA = PSA.newPSA2D(
                compressedGrid.grid(),
                Integer.class,
                0,
                compressedGrid::get,
                Integer::sum,
                (a, b) -> a - b);

        return loop.pairPoints()
                .filter(pair -> isRectangleFullyInside(gridPSA, loop, pair.left(), pair.right()))
                .map(pair -> rectangleArea(pair.left(), pair.right()))
                .mapToLong(Long::longValue)
                .max()
                .orElse(0);
    }

    private long rectangleArea(final Position a, final Position b) {
        return (long) (Math.abs(a.x() - b.x()) + 1) * (Math.abs(a.y() - b.y()) + 1);
    }

    private Grid<Integer> compressGrid(final Loop loop) {
        final Integer[][] grid = new Integer[loop.sortedX.size() * 2 - 1][loop.sortedY.size() * 2 - 1];
        Util.loop2D(grid, (e, i, j) -> grid[i][j] = 0);

        IntStream.range(0, loop.closedLoop.size() - 1)
                .forEach(i -> {
                    final int x1 = loop.closedLoop.get(i).x();
                    final int y1 = loop.closedLoop.get(i).y();
                    final int x2 = loop.closedLoop.get(i + 1).x();
                    final int y2 = loop.closedLoop.get(i + 1).y();

                    final CompressedPair compressedPair = CompressedPair.of(loop, x1, y1, x2, y2);

                    IntStream.range(compressedPair.x1(), compressedPair.x2() + 1)
                            .forEach(cx -> IntStream.range(compressedPair.y1(), compressedPair.y2() + 1)
                                    .forEach(cy -> grid[cx][cy] = 1));
                });

        return Grid.of(grid, -1);
    }

    private void floodFill(final Grid<Integer> grid) {
        final Function<Position, List<Position>> neighborGetter = pos -> List.of(
                pos.apply(Util.Offset.UP),
                pos.apply(Util.Offset.RIGHT),
                pos.apply(Util.Offset.DOWN),
                pos.apply(Util.Offset.LEFT));

        final Position start = Position.of(-1, -1);
        final Set<Position> outside = new HashSet<>();
        outside.add(start);
        final Queue<Position> flood = new ArrayDeque<>();
        flood.add(start);
        while(!flood.isEmpty()) {
            final Position current = flood.poll();
            for (final Position next : neighborGetter.apply(current)) {
                if (next.x() < -1 || next.y() < -1 || next.x() > grid.cols() || next.y() > grid.rows()) {
                    continue;
                }
                if (grid.get(next) == 1) {
                    continue;
                }
                if (outside.contains(next)) {
                    continue;
                }
                outside.add(next);
                flood.add(next);
            }
        }

        Util.loop2D(grid, (e, i, j) -> {
            if (!outside.contains(Position.of(i, j))) {
                grid.set(i, j, 1);
            }
        });
    }

    private boolean isRectangleFullyInside(PSA<Integer[][]> psa, final Loop loop, Position point1, final Position point2) {
        final CompressedPair compressedPair = CompressedPair.of(loop, point1.x(), point1.y(), point2.x(), point2.y());
        final int left = compressedPair.x1() > 0 ? psa.psa()[compressedPair.x1() - 1][compressedPair.y2()] : 0;
        final int top = compressedPair.y1() > 0 ? psa.psa()[compressedPair.x2()][compressedPair.y1() - 1] : 0;
        final int topLeft = compressedPair.x1() > 0 && compressedPair.y1() > 0 ? psa.psa()[compressedPair.x1() - 1][compressedPair.y1() - 1] : 0;
        final int tiles = psa.psa()[compressedPair.x2()][compressedPair.y2()] - left - top + topLeft;
        return (long) (Math.abs(compressedPair.x1() - compressedPair.x2()) + 1) * (Math.abs(compressedPair.y1() - compressedPair.y2()) + 1) == tiles;
    }

    private record Loop(List<Position> points, List<Position> closedLoop, List<Integer> sortedX, List<Integer> sortedY) {
        private static Loop parse(final String input) {
            final List<Position> points = input.lines()
              .map(line -> line.split(","))
              .map(parts -> Position.of(Integer.parseInt(parts[0]), Integer.parseInt(parts[1])))
              .toList();
            final List<Position> closedLoop = Stream.concat(points.stream(), Stream.of(points.getFirst())).toList();
            final List<Integer> sortedX = points.stream()
                    .map(Position::x)
                    .distinct()
                    .sorted()
                    .toList();
            final List<Integer> sortedY = points.stream()
                    .map(Position::y)
                    .distinct()
                    .sorted()
                    .toList();
            return new Loop(points, closedLoop, sortedX, sortedY);
        }

        private Stream<Pair<Position, Position>> pairPoints() {
            return IntStream.range(0, points.size() - 1)
                    .mapToObj(i -> IntStream.range(i + 1, points.size())
                            .mapToObj(j -> Pair.of(points.get(i), points.get(j))))
                    .flatMap(Function.identity());
        }
    }

    private record CompressedPair(int x1, int y1, int x2, int y2) {
        private static CompressedPair of(final Loop loop, final int x1, final int y1, final int x2, final int y2) {
            final List<Integer> cxs = Stream.of(loop.sortedX.indexOf(x1) * 2, loop.sortedX.indexOf(x2) * 2)
                    .sorted()
                    .toList();
            final List<Integer> cys = Stream.of(loop.sortedY.indexOf(y1) * 2, loop.sortedY.indexOf(y2) * 2)
                    .sorted()
                    .toList();

            return new CompressedPair(cxs.getFirst(), cys.getFirst(), cxs.getLast(), cys.getLast());
        }
    }
}
