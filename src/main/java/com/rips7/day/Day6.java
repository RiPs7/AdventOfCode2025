package com.rips7.day;

import processing.core.PApplet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Day6 implements Day<Long> {

    private List<VisualProblem> visualProblems;
    private long grandTotal;
    private AnimationPhase phase = AnimationPhase.SETUP;
    private int animationStep = 0;
    private boolean isPart2Setup = false;
    private float cameraY = 0;

    private enum AnimationPhase {
        SETUP, PARSING, SOLVING, DONE
    }

    @Override
    public Long part1(String input) {
        return parseProblems(input, false).stream()
                .mapToLong(Problem::solve)
                .sum();
    }

    @Override
    public Long part2(String input) {
        return parseProblems(input, true).stream()
                .mapToLong(Problem::solve)
                .sum();
    }

    @Override
    public boolean visualize() {
        return Day.super.visualize();
//        return true;
    }

    @Override
    public void setupVisuals(PApplet canvas, String input) {
        visualProblems = new ArrayList<>();
        grandTotal = 0;
        animationStep = 0;
        phase = AnimationPhase.SETUP;
        cameraY = 0;
        canvas.frameRate(10);
    }

    @Override
    public boolean drawPart1(PApplet canvas) {
        return runVisualization(canvas, "Part 1", false);
    }

    @Override
    public boolean drawPart2(PApplet canvas) {
        if (!isPart2Setup) {
            setupVisuals(canvas, Day.super.loadInput());
            isPart2Setup = true;
        }
        return runVisualization(canvas, "Part 2", true);
    }

    private boolean runVisualization(PApplet canvas, String title, boolean rightToLeft) {
        drawBackground(canvas, title);
        drawProblems(canvas);
        drawGrandTotal(canvas);

        switch (phase) {
            case SETUP:
                if (animationStep++ > 10) {
                    phase = AnimationPhase.PARSING;
                    animationStep = 0;
                }
                break;
            case PARSING:
                List<Problem> parsedProblems = parseProblems(Day.super.loadInput(), rightToLeft);
                visualProblems = parsedProblems.stream().map(VisualProblem::new).collect(Collectors.toList());
                phase = AnimationPhase.SOLVING;
                animationStep = 0;
                break;
            case SOLVING:
                if (animationStep < visualProblems.size()) {
                    VisualProblem problem = visualProblems.get(animationStep);
                    problem.solve();
                    grandTotal += problem.result;
                    animationStep++;
                } else {
                    phase = AnimationPhase.DONE;
                }
                break;
            case DONE:
                return false;
        }
        return true;
    }

    private void drawBackground(PApplet canvas, String title) {
        canvas.background(0);
        canvas.fill(255);
        canvas.textSize(24);
        canvas.text(title, canvas.width / 2f, 30);
    }

    private void drawProblems(PApplet canvas) {
        float padding = 10;
        int problemsPerRow = 4;
        float problemWidth = (canvas.width - (problemsPerRow + 1) * padding) / problemsPerRow;
        float problemHeight = 100;
        float startY = canvas.height / 2f - problemHeight;

        int currentRow = (animationStep >= visualProblems.size()) ? (visualProblems.size() - 1) / problemsPerRow : animationStep / problemsPerRow;
        float targetY = startY - (currentRow * (problemHeight + padding));
        cameraY += (targetY - cameraY) * 0.1f;

        canvas.pushMatrix();
        canvas.translate(0, cameraY);

        for (int i = 0; i < visualProblems.size(); i++) {
            int row = i / problemsPerRow;
            int col = i % problemsPerRow;
            float x = padding + col * (problemWidth + padding);
            float y = row * (problemHeight + padding);

            VisualProblem vp = visualProblems.get(i);
            canvas.stroke(100);
            canvas.noFill();
            canvas.rect(x, y, problemWidth, problemHeight);

            if (phase == AnimationPhase.SOLVING && i == animationStep) {
                canvas.stroke(255, 255, 0);
                canvas.strokeWeight(3);
                canvas.rect(x - 2, y - 2, problemWidth + 4, problemHeight + 4);
                canvas.strokeWeight(1);
            }

            canvas.fill(255);
            canvas.textSize(12);
            String numbers = vp.problem.numbers().stream().map(String::valueOf).collect(Collectors.joining(" " + vp.getOperationSymbol() + " "));
            canvas.text(numbers, x + 20, y + 30);

            if (vp.isSolved) {
                canvas.fill(0, 255, 0);
                canvas.textSize(16);
                canvas.text("= " + vp.result, x + 20, y + 70);
            }
        }
        canvas.popMatrix();
    }

    private void drawGrandTotal(PApplet canvas) {
        canvas.fill(0, 255, 0);
        canvas.textSize(32);
        canvas.text("Grand Total: " + grandTotal, canvas.width / 2f, canvas.height - 40);
    }

    private static class VisualProblem {
        Problem problem;
        long result;
        boolean isSolved = false;
        VisualProblem(Problem problem) { this.problem = problem; }
        void solve() {
            this.result = problem.solve();
            this.isSolved = true;
        }
        char getOperationSymbol() {
            return (problem.accumulator.apply(1L, 2L) == 3) ? '+' : '*';
        }
    }

    private List<Problem> parseProblems(final String input, final boolean rightToLeft) {
        final String[] lines = input.lines().toArray(String[]::new);
        final String[] lineNumbers = Arrays.copyOf(lines, lines.length - 1);
        final List<List<Long>> numbers = rightToLeft ?
                parseRightToLeft(lineNumbers) :
                parseLeftToRight(lineNumbers);
        final List<Character> operations = Arrays.stream(lines[lines.length - 1].split("\\s+"))
                .map(e -> e.charAt(0))
                .toList();
        return IntStream.range(0, numbers.size())
                .mapToObj(i -> new Problem(numbers.get(i), operations.get(i)))
                .toList();
    }

    private List<List<Long>> parseLeftToRight(final String[] lines) {
        final List<List<Long>> inlineNumbers = Arrays.stream(lines)
                .map(line -> Arrays.stream(line.trim().split("\\s+"))
                        .map(Long::parseLong)
                        .toList())
                .toList();
        return IntStream.range(0, inlineNumbers.getFirst().size())
                .mapToObj(col -> inlineNumbers.stream()
                        .map(row -> row.get(col))
                        .toList())
                .toList();
    }

    private List<List<Long>> parseRightToLeft(final String[] lines) {
        final int maxWidth = Arrays.stream(lines).mapToInt(String::length).max().orElse(0);
        final List<String> paddedLines = Arrays.stream(lines)
                .map(line -> String.format("%-" + maxWidth + "s", line))
                .toList();

        final List<String> columns = IntStream.range(0, maxWidth)
                .mapToObj(c -> paddedLines.stream()
                        .map(row -> String.valueOf(row.charAt(c)))
                        .collect(Collectors.joining()))
                .collect(Collectors.toList());

        Collections.reverse(columns);

        final String allProblemsString = String.join("|", columns);
        final List<List<Long>> problemNumbers = Arrays.stream(allProblemsString.split("\\|\\s+\\|"))
                .map(problemString -> Arrays.stream(problemString.split("\\|"))
                        .map(s -> s.replaceAll("\\s", ""))
                        .filter(s -> !s.isEmpty())
                        .map(Long::parseLong)
                        .toList())
                .collect(Collectors.toList());

        Collections.reverse(problemNumbers);

        return problemNumbers;
    }

    private record Problem(List<Long> numbers, BinaryOperator<Long> accumulator) {
        private Problem(final List<Long> numbers, final char operation) {
            this(numbers, getAccumulator(operation));
        }

        private static BinaryOperator<Long> getAccumulator(final char operation) {
            return switch (operation) {
                case '+' -> Long::sum;
                case '*' -> (a, b) -> a * b;
                default -> throw new RuntimeException();
            };
        }

        private Long solve() {
            return numbers.stream()
                    .reduce(accumulator)
                    .orElseThrow();
        }
    }
}
