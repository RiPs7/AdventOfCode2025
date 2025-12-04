package com.rips7.day;

import processing.core.PApplet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Day3 implements Day<Long> {

    private static final int STEPS_PER_FRAME = 10;

    // --- Visualization State ---
    private List<VisualRank> visualRanks;
    private int rankIndex;
    private int digitIndex;
    private long totalSum;
    private boolean isPart2Setup = false;
    private int maxBatteriesInRank;
    private float cameraY = 0;

    private final List<VisualDigit> resultDigits = new ArrayList<>();
    private int removalsLeft;
    private AnimationSubStep subStep = AnimationSubStep.START_RANK;

    private enum AnimationSubStep {
        START_RANK,
        SELECT_DIGIT,
        COMPARE_AND_DISCARD,
        ADD_TO_RESULT,
        FINALIZE_RANK
    }

    @Override
    public Long part1(String input) {
        return input.lines()
            .map(BatteryRank::parse)
            .mapToLong(rank -> rank.findLargestJoltageOf(2))
            .sum();
    }

    @Override
    public Long part2(String input) {
        return input.lines()
            .map(BatteryRank::parse)
            .mapToLong(rank -> rank.findLargestJoltageOf(12))
            .sum();
    }

    @Override
    public boolean visualize() {
        return Day.super.visualize();
//         return true;
    }

    @Override
    public void settings(PApplet pApplet) {
        pApplet.size(1200, 800);
    }

    @Override
    public void setupVisuals(PApplet canvas, String input) {
        this.visualRanks = input.lines().map(VisualRank::parse).toList();
        this.maxBatteriesInRank = visualRanks.stream()
                .mapToInt(r -> r.digits().size())
                .max()
                .orElse(1);
        this.rankIndex = 0;
        this.digitIndex = 0;
        this.totalSum = 0;
        this.subStep = AnimationSubStep.START_RANK;
        canvas.textAlign(PApplet.CENTER, PApplet.CENTER);
        canvas.frameRate(60);
    }

    @Override
    public boolean drawPart1(PApplet canvas) {
        for (int i = 0; i < STEPS_PER_FRAME; i++) {
            if (!runVisualizationStep(2)) return false;
        }
        drawFrame(canvas, "Part 1");
        return true;
    }

    @Override
    public boolean drawPart2(PApplet canvas) {
        if (!isPart2Setup) {
            setupVisuals(canvas, Day.super.loadInput());
            isPart2Setup = true;
        }
        for (int i = 0; i < STEPS_PER_FRAME; i++) {
            if (!runVisualizationStep(12)) return false;
        }
        drawFrame(canvas, "Part 2");
        return true;
    }

    private void drawFrame(PApplet canvas, String title) {
        drawBackground(canvas, title);
        drawRanks(canvas);
        drawTotalSum(canvas);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean runVisualizationStep(int requiredBatteries) {
        if (rankIndex >= visualRanks.size()) {
            return false;
        }

        VisualRank currentRank = visualRanks.get(rankIndex);

        switch (subStep) {
            case START_RANK:
                resultDigits.clear();
                removalsLeft = currentRank.digits().size() - requiredBatteries;
                digitIndex = 0;
                currentRank.digits().forEach(d -> d.setState(DigitState.NORMAL));
                subStep = AnimationSubStep.SELECT_DIGIT;
                break;

            case SELECT_DIGIT:
                if (digitIndex >= currentRank.digits().size()) {
                    subStep = AnimationSubStep.FINALIZE_RANK;
                    break;
                }
                currentRank.digits().get(digitIndex).setState(DigitState.TESTING);
                subStep = AnimationSubStep.COMPARE_AND_DISCARD;
                break;

            case COMPARE_AND_DISCARD:
                VisualDigit testingDigit = currentRank.digits().get(digitIndex);
                if (!resultDigits.isEmpty() && testingDigit.value() > resultDigits.getLast().value() && removalsLeft > 0) {
                    VisualDigit removed = resultDigits.removeLast();
                    removed.setState(DigitState.DISCARDED);
                    removalsLeft--;
                } else {
                    subStep = AnimationSubStep.ADD_TO_RESULT;
                }
                break;

            case ADD_TO_RESULT:
                VisualDigit digitToAdd = currentRank.digits().get(digitIndex);
                resultDigits.add(digitToAdd);
                resultDigits.forEach(d -> d.setState(DigitState.KEPT));
                digitIndex++;
                subStep = AnimationSubStep.SELECT_DIGIT;
                break;

            case FINALIZE_RANK:
                while (resultDigits.size() > requiredBatteries) {
                    resultDigits.removeLast().setState(DigitState.DISCARDED);
                }
                Set<VisualDigit> keptDigits = new HashSet<>(resultDigits);
                for (VisualDigit d : currentRank.digits()) {
                    if (!keptDigits.contains(d)) {
                        d.setState(DigitState.DISCARDED);
                    }
                }
                totalSum += Long.parseLong(resultDigits.stream().map(d -> String.valueOf(d.value())).collect(Collectors.joining()));
                rankIndex++;
                subStep = AnimationSubStep.START_RANK;
                break;
        }
        return true;
    }

    private void drawBackground(PApplet canvas, String title) {
        canvas.background(0);
        canvas.fill(255);
        canvas.textSize(24);
        canvas.text(title, canvas.width / 2f, 30);
    }

    private void drawRanks(PApplet canvas) {
        float padding = 50;
        float availableWidth = canvas.width - (padding * 2);
        float cellWidth = availableWidth / maxBatteriesInRank;
        float rectSize = cellWidth * 0.8f;
        float spacing = cellWidth * 0.2f;
        float rowHeight = rectSize + 10;

        // Corrected camera logic for natural downward scroll
        float targetY = -rankIndex * rowHeight + canvas.height / 2f - rowHeight / 2f;
        cameraY += (targetY - cameraY) * 0.1f;

        canvas.pushMatrix();
        canvas.translate(0, cameraY);

        float y = 0;
        for (int i = 0; i < visualRanks.size(); i++) {
            VisualRank rank = visualRanks.get(i);
            float x = padding;
            for (VisualDigit digit : rank.digits()) {
                canvas.stroke(100);
                canvas.fill(digit.state().color(canvas));
                canvas.rect(x, y, rectSize, rectSize, rectSize / 8f);
                canvas.fill(0);
                canvas.textSize(rectSize * 0.5f);
                canvas.text(digit.value(), x + rectSize / 2f, y + rectSize / 2f);
                x += rectSize + spacing;
            }
            if (i == rankIndex) {
                canvas.noFill();
                canvas.stroke(255, 255, 0);
                canvas.strokeWeight(2);
                canvas.rect(padding - 5, y - 5, x - padding + 5, rectSize + 10, (rectSize / 8f) + 2);
                canvas.strokeWeight(1);
            }
            y += rowHeight;
        }
        canvas.popMatrix();
    }

    private void drawTotalSum(PApplet canvas) {
        canvas.fill(0, 255, 0);
        canvas.textSize(32);
        canvas.text("Total Joltage: " + totalSum, canvas.width / 2f, canvas.height - 40);
    }

    private enum DigitState {
        NORMAL(150),
        TESTING(100, 100, 255),
        KEPT(255, 215, 0),
        DISCARDED(139, 0, 0);

        private final int r, g, b;
        DigitState(int gray) { this(gray, gray, gray); }
        DigitState(int r, int g, int b) { this.r = r; this.g = g; this.b = b; }
        public int color(PApplet canvas) { return canvas.color(r, g, b); }
    }

    private static class VisualDigit {
        private final int value;
        private DigitState state = DigitState.NORMAL;
        public VisualDigit(int value) { this.value = value; }
        public int value() { return value; }
        public DigitState state() { return state; }
        public void setState(DigitState state) { this.state = state; }
    }

    private record VisualRank(List<VisualDigit> digits) {
        public static VisualRank parse(String input) {
            return new VisualRank(input.chars()
                .mapToObj(c -> new VisualDigit(Character.getNumericValue(c)))
                .toList());
        }
    }

    private record BatteryRank(Integer[] batteries) {
        private static BatteryRank parse(final String input) {
            return new BatteryRank(Arrays.stream(input.split("")).map(Integer::parseInt).toArray(Integer[]::new));
        }

        private long findLargestJoltageOf(int requiredBatteries) {
            final List<Integer> resultDigits = new ArrayList<>(requiredBatteries);
            int removalsLeft = batteries.length - requiredBatteries;
            for (final int digit : batteries) {
                while (!resultDigits.isEmpty() && digit > resultDigits.getLast() && removalsLeft > 0) {
                    resultDigits.removeLast();
                    removalsLeft--;
                }
                resultDigits.add(digit);
            }
            while (resultDigits.size() > requiredBatteries) {
                resultDigits.removeLast();
            }
            return Long.parseLong(resultDigits.stream().map(String::valueOf).collect(Collectors.joining()));
        }
    }
}
