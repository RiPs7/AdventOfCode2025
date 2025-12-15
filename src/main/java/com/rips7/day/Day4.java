package com.rips7.day;

import com.rips7.util.Util.Grid;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Day4 implements Day<Integer> {

    @Override
    public Integer part1(String input) {
        final Grid<Cell> grid = parseGrid(input);
        final AtomicInteger count = new AtomicInteger();
        grid.convolutionFull((cell, neighbors) -> {
            if (neighbors.stream().filter(Cell::isRoll).count() < 4) {
                count.incrementAndGet();
            }
        }, Cell::isRoll);
        return count.get();
    }

    @Override
    public Integer part2(String input) {
        final Grid<Cell> grid = parseGrid(input);
        final AtomicInteger removedCount = new AtomicInteger();
        final AtomicBoolean changed = new AtomicBoolean(true);
        while (changed.get()) {
            changed.set(false);
            grid.convolutionFull((cell, neighbors) -> {
                if (neighbors.stream().filter(Cell::isRoll).count() < 4) {
                    cell.setIsRoll(false);
                    removedCount.incrementAndGet();
                    changed.set(true);
                }
            }, Cell::isRoll);
        }
        return removedCount.get();
    }

    private static Grid<Cell> parseGrid(final String input) {
        final Cell[][] grid = input.lines()
            .map(line -> line.chars()
                .mapToObj(c -> Cell.of((char) c))
                .toArray(Cell[]::new))
            .toArray(Cell[][]::new);
        return Grid.of(grid);
    }

    private static final class Cell {
        private boolean isRoll;

        private static Cell of(final char c) {
            return new Cell(c == '@');
        }

        public Cell(final boolean isRoll) {
            this.isRoll = isRoll;
        }

        private boolean isRoll() { return isRoll; }

        public void setIsRoll(boolean roll) { isRoll = roll; }
    }
}
