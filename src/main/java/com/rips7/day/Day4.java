package com.rips7.day;

import com.rips7.util.Util.Grid;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Day4 implements Day<Integer> {

    @Override
    public Integer part1(String input) {
        final Grid<Cell> grid = parseGrid(input);
        final AtomicInteger accessibleRolls = new AtomicInteger();
        grid.convolutionFull((cell, neighbors) -> {
            final long neighboringRolls = neighbors.stream()
                    .filter(Cell::isRoll)
                    .count();
            if (neighboringRolls < 4) {
                accessibleRolls.incrementAndGet();
            }
        }, Cell::isRoll);
        return accessibleRolls.get();
    }

    @Override
    public Integer part2(String input) {
        final Grid<Cell> grid = parseGrid(input);

        final AtomicInteger rollsRemoved = new AtomicInteger();
        final AtomicBoolean rollRemoved = new AtomicBoolean();

        do {
            rollRemoved.set(false);
            grid.convolutionFull((cell, neighbors) -> {
                final long neighboringRolls = neighbors.stream()
                        .filter(Cell::isRoll)
                        .count();
                if (neighboringRolls < 4) {
                    cell.setValue('x');
                    cell.setIsRoll(false);
                    rollsRemoved.incrementAndGet();
                    rollRemoved.set(true);
                }
            }, Cell::isRoll);
        } while (rollRemoved.get());

        return rollsRemoved.get();
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
        private char value;
        private boolean isRoll;

        private static Cell of(final char c) {
            return new Cell(c, c == '@');
        }

        public void setValue(char value) {
            this.value = value;
        }

        private boolean isRoll() {
            return isRoll;
        }

        public void setIsRoll(boolean roll) {
            isRoll = roll;
        }

        public Cell(char value, boolean isRoll) {
            this.value = value;
            this.isRoll = isRoll;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

}
