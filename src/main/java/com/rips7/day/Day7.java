package com.rips7.day;

import com.rips7.util.Util.Grid;
import com.rips7.util.Util.Offset;
import com.rips7.util.Util.Position;
import com.rips7.util.algorithms.pathfinding.DFS;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class Day7 implements Day<Long> {
    private static final char GRID_START = 'S';
    private static final char GRID_EMPTY = '.';
    private static final char GRID_SPLIT = '^';
    private static final char GRID_END = '$';

    @Override
    public Long part1(String input) {
        return countSplits(parseGrid(input));
    }

    @Override
    public Long part2(String input) {
        return countTimelines(parseGrid(input));
    }

    private Grid<Character> parseGrid(final String input) {
        final Character[][] grid = input.lines()
          .map(line -> line.chars()
            .mapToObj(c -> (char) c)
            .toArray(Character[]::new))
          .toArray(Character[][]::new);
        return Grid.of(grid, GRID_END);
    }

    private long countSplits(final Grid<Character> grid) {
        final AtomicLong splits = new AtomicLong();

        new DFS<Position>().run(grid.find(GRID_START), pos ->
                switch (grid.get(pos)) {
                    case GRID_END -> List.of();
                    case GRID_START, GRID_EMPTY -> List.of(pos.apply(Offset.DOWN));
                    case GRID_SPLIT -> {
                        splits.incrementAndGet();
                        yield List.of(pos.apply(Offset.LEFT), pos.apply(Offset.RIGHT));
                    }
                    default -> throw new RuntimeException("Shouldn't happen");
                });

        return splits.get();
    }

    private long countTimelines(final Grid<Character> grid) {
        // Dynamic programming
        final Position start = grid.find(GRID_START);
        final Long[][] memo = new Long[grid.rows()][grid.cols()];
        return countTimelines(grid, start, memo);
    }

    private long countTimelines(final Grid<Character> grid, final Position position, final Long[][] memo) {
        // If it goes off the sides of the manifold, the path is invalid
        if (!grid.isWithin(position)) {
            return 0;
        }
        // If it reaches the last row, the path is valid
        if (position.x() == grid.cols() - 1) {
            return 1;
        }
        // If we have already calculated the timelines for the current position, return it
        if (memo[position.x()][position.y()] != null) {
            return memo[position.x()][position.y()];
        }
        // Determine next position and calculate total paths
        final char next = grid.get(position.apply(Offset.DOWN));
        final long totalPaths;
        if (next == GRID_SPLIT) {
            // If the path splits, we calculate the total paths as the sum of left and right paths
            final long pathsLeft = countTimelines(grid, position.apply(Offset.LEFT), memo);
            final long pathsRight = countTimelines(grid, position.apply(Offset.RIGHT), memo);
            totalPaths = pathsLeft + pathsRight;
        } else {
            // Move further down the path
            totalPaths = countTimelines(grid, position.apply(Offset.DOWN), memo);
        }
        // Memoize the result and return it
        memo[position.x()][position.y()] = totalPaths;
        return totalPaths;
    }
}
