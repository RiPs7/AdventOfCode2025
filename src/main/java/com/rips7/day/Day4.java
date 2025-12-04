package com.rips7.day;

import com.rips7.util.Util.Grid;
import processing.core.PApplet;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Day4 implements Day<Integer> {

    private final List<Cell> cellsToChange = new ArrayList<>();
    private Grid<Cell> visualGrid;
    private int animationStep;
    private int accessibleRolls;
    private int rollsRemoved;
    private boolean isPart2Setup = false;

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

    @Override
    public boolean visualize() {
        return Day.super.visualize();
//        return true;
    }

    @Override
    public void settings(PApplet pApplet) {
        pApplet.size(1200, 800);
    }

    @Override
    public void setupVisuals(PApplet canvas, String input) {
        this.visualGrid = parseGrid(input);
        this.animationStep = 0;
        this.accessibleRolls = 0;
        this.rollsRemoved = 0;
        canvas.frameRate(60);
    }

    @Override
    public boolean drawPart1(PApplet canvas) {
        if (animationStep >= visualGrid.rows() * visualGrid.cols()) {
            return false;
        }
        drawBackground(canvas, "Part 1");
        drawGrid(canvas);
        drawCounters(canvas);

        int row = animationStep / visualGrid.cols();
        int col = animationStep % visualGrid.cols();
        Cell cell = visualGrid.get(row, col);

        if (cell.isRoll()) {
            cell.state = CellState.TESTING;
            long neighboringRolls = visualGrid.getNeighborsFull(row, col).stream()
                    .filter(Cell::isRoll)
                    .count();
            if (neighboringRolls < 4) {
                cell.state = CellState.ACCESSIBLE;
                accessibleRolls++;
            }
        }
        animationStep++;
        return true;
    }

    @Override
    public boolean drawPart2(PApplet canvas) {
        if (!isPart2Setup) {
            setupVisuals(canvas, Day.super.loadInput());
            isPart2Setup = true;
            canvas.frameRate(30);
        }

        drawBackground(canvas, "Part 2");
        drawGrid(canvas);
        drawCounters(canvas);

        if (cellsToChange.isEmpty()) {
            animationStep++;
            visualGrid.iterate(cell -> {
                if (cell.state == CellState.REMOVING) cell.state = CellState.EMPTY;
            });

            visualGrid.convolutionFull((cell, neighbors) -> {
                if (neighbors.stream().filter(Cell::isRoll).count() < 4) {
                    cellsToChange.add(cell);
                }
            }, Cell::isRoll);

            if (cellsToChange.isEmpty()) {
                return false;
            }
            cellsToChange.forEach(c -> c.state = CellState.REMOVING);
        } else {
            for (Cell cell : cellsToChange) {
                cell.setIsRoll(false);
                rollsRemoved++;
            }
            cellsToChange.clear();
        }
        return true;
    }

    private void drawBackground(PApplet canvas, String title) {
        canvas.background(0);
        canvas.fill(255);
        canvas.textSize(24);
        canvas.text(title, canvas.width / 2f, 30);
    }

    private void drawGrid(PApplet canvas) {
        float topMargin = 60;
        float bottomMargin = 80;
        float availableHeight = canvas.height - topMargin - bottomMargin;
        float availableWidth = canvas.width - 40;

        float cellSize = PApplet.min(availableWidth / visualGrid.cols(), availableHeight / visualGrid.rows());
        float gridWidth = visualGrid.cols() * cellSize;
        float gridHeight = visualGrid.rows() * cellSize;
        float startX = (canvas.width - gridWidth) / 2;
        float startY = topMargin + (availableHeight - gridHeight) / 2;

        for (int r = 0; r < visualGrid.rows(); r++) {
            for (int c = 0; c < visualGrid.cols(); c++) {
                Cell cell = visualGrid.get(r, c);
                canvas.fill(cell.getCurrentColor(canvas));
                canvas.noStroke();
                canvas.rect(startX + c * cellSize, startY + r * cellSize, cellSize, cellSize);
            }
        }
    }

    private void drawCounters(PApplet canvas) {
        canvas.textSize(28);
        if (!isPart2Setup) {
            canvas.fill(0, 255, 0);
            canvas.text("Accessible: " + accessibleRolls, canvas.width / 2f, canvas.height - 30);
        } else {
            canvas.fill(255, 0, 0);
            canvas.text("Removed: " + rollsRemoved, canvas.width / 2f, canvas.height - 30);
            canvas.fill(255, 255, 0);
            canvas.text("Round: " + animationStep, 100, canvas.height - 30);
        }
    }

    private static Grid<Cell> parseGrid(final String input) {
        final Cell[][] grid = input.lines()
            .map(line -> line.chars()
                .mapToObj(c -> Cell.of((char) c))
                .toArray(Cell[]::new))
            .toArray(Cell[][]::new);
        return Grid.of(grid);
    }

    private enum CellState {
        ROLL, EMPTY, TESTING, ACCESSIBLE, REMOVING
    }

    private static final class Cell {
        private boolean isRoll;
        private CellState state;

        private static Cell of(final char c) {
            return new Cell(c == '@');
        }

        public Cell(final boolean isRoll) {
            this.isRoll = isRoll;
            this.state = isRoll ? CellState.ROLL : CellState.EMPTY;
        }

        public int getCurrentColor(PApplet canvas) {
            return switch (state) {
                case ROLL -> canvas.color(255, 215, 0);
                case EMPTY -> canvas.color(40);
                case TESTING -> canvas.color(100, 100, 255);
                case ACCESSIBLE -> canvas.color(0, 255, 0);
                case REMOVING -> canvas.color(255, 0, 0);
            };
        }

        private boolean isRoll() { return isRoll; }

        public void setIsRoll(boolean roll) { isRoll = roll; }
    }
}
