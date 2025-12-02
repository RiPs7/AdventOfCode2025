package com.rips7.day;

import processing.core.PApplet;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Day2 implements Day<Long> {

    // --- Visualization Fields ---
    private List<Range> ranges;
    private long maxRangeEnd;
    private long totalSum;
    private int animationStep; // Represents the outer loop (d or lenS)
    private int innerAnimationStep; // Represents the inner loop (lenP)
    private boolean isPart2Setup = false;

    @Override
    public Long part1(String input) {
        return Arrays.stream(input.split(","))
                .map(Range::parse)
                .map(RangePart1::new)
                .mapToLong(RangePart1::sumInvalidIds)
                .sum();
    }

    @Override
    public Long part2(String input) {
        return Arrays.stream(input.split(","))
                .map(Range::parse)
                .map(RangePart2::new)
                .mapToLong(RangePart2::sumInvalidIds)
                .sum();
    }

    @Override
    public boolean visualize() {
        return Day.super.visualize();
//         return true;
    }

    @Override
    public void setupVisuals(PApplet canvas, String input) {
        this.ranges = Arrays.stream(input.split(",")).map(Range::parse).toList();
        this.maxRangeEnd = ranges.stream().mapToLong(Range::end).max().orElse(1);
        this.totalSum = 0;
        this.animationStep = 1;
        this.innerAnimationStep = 1;
        canvas.textAlign(PApplet.CENTER, PApplet.CENTER);
    }

    @Override
    public boolean drawPart1(PApplet canvas) {
        if (animationStep > 8) { // d from 1 to 8
            return false; // Part 1 animation is done
        }

        drawBackground(canvas, "Part 1: Scanning by Digit Length (d)");
        drawNumberLine(canvas);
        drawTotalSum(canvas);

        // --- Algorithm Visualization ---
        int d = animationStep;
        long powerOf10 = (long) Math.pow(10, d);
        long combinator = powerOf10 + 1;

        // Visualize the current search
        canvas.fill(255, 255, 0, 50); // Yellow tint for search area
        canvas.rect(0, 100, canvas.width, canvas.height - 200);
        canvas.fill(255, 255, 0);
        canvas.text("d = " + d, canvas.width / 2f, 120);

        // Process one digit length 'd' per animation cycle
        for (Range range : ranges) {
            long minF = (long) Math.pow(10, d - 1);
            long maxF = powerOf10 - 1;
            long lowerBoundF = (range.start() + combinator - 1) / combinator;
            long upperBoundF = range.end() / combinator;
            long actualMinF = Math.max(minF, lowerBoundF);
            long actualMaxF = Math.min(maxF, upperBoundF);

            if (actualMinF <= actualMaxF) {
                long numTerms = actualMaxF - actualMinF + 1;
                long sumOfF = numTerms * (actualMinF + actualMaxF) / 2;
                totalSum += sumOfF * combinator;

                // Flash for found range
                float x1 = PApplet.map((float) Math.log10(actualMinF * combinator), 0, (float) Math.log10(maxRangeEnd), 0, canvas.width);
                float x2 = PApplet.map((float) Math.log10(actualMaxF * combinator), 0, (float) Math.log10(maxRangeEnd), 0, canvas.width);
                canvas.fill(0, 255, 0, 150);
                canvas.rect(x1, canvas.height - 100, x2 - x1, 50);
            }
        }

        animationStep++;
        canvas.frameRate(1);
        return true;
    }

    @Override
    public boolean drawPart2(PApplet canvas) {
        if (!isPart2Setup) {
            totalSum = 0;
            animationStep = 2; // Start lenS from 2
            innerAnimationStep = 1;
            isPart2Setup = true;
        }

        int endLen = String.valueOf(maxRangeEnd).length();
        if (animationStep > endLen) {
            return false; // Part 2 animation is done
        }

        drawBackground(canvas, "Part 2: Scanning by Total Length (lenS) and Prefix Length (lenP)");
        drawNumberLine(canvas);
        drawTotalSum(canvas);

        // --- Algorithm Visualization ---
        int lenS = animationStep;
        int lenP = innerAnimationStep;

        if (lenS % lenP == 0) {
            long powerOf10_S = (long) Math.pow(10, lenS);
            long powerOf10_P = (long) Math.pow(10, lenP);
            long combinator = (powerOf10_S - 1) / (powerOf10_P - 1);

            // Visualize search
            canvas.fill(255, 255, 0, 50);
            canvas.rect(0, 100, canvas.width, canvas.height - 200);
            canvas.fill(255, 255, 0);
            canvas.text("lenS = " + lenS + ", lenP = " + lenP, canvas.width / 2f, 120);

            for (Range range : ranges) {
                long minP = (long) Math.pow(10, lenP - 1);
                long maxP = powerOf10_P - 1;
                long lowerBoundP = (range.start() + combinator - 1) / combinator;
                long upperBoundP = range.end() / combinator;
                long actualMinP = Math.max(minP, lowerBoundP);
                long actualMaxP = Math.min(maxP, upperBoundP);

                Set<Long> foundIds = new HashSet<>();
                for (long p = actualMinP; p <= actualMaxP; p++) {
                    foundIds.add(p * combinator);
                }
                totalSum += foundIds.stream().mapToLong(Long::longValue).sum();
            }
        }

        // --- Animation Control ---
        innerAnimationStep++;
        if (innerAnimationStep > lenS / 2) {
            innerAnimationStep = 1;
            animationStep++;
        }
        canvas.frameRate(5);
        return true;
    }

    private void drawBackground(PApplet canvas, String title) {
        canvas.background(0);
        canvas.fill(255);
        canvas.textSize(24);
        canvas.text(title, canvas.width / 2f, 50);
    }

    private void drawNumberLine(PApplet canvas) {
        float y = canvas.height - 75;
        canvas.stroke(255);
        canvas.line(0, y, canvas.width, y);

        // Draw logarithmic scale markers
        for (int i = 0; i <= Math.log10(maxRangeEnd); i++) {
            float x = PApplet.map(i, 0, (float) Math.log10(maxRangeEnd), 0, canvas.width);
            canvas.stroke(255);
            canvas.line(x, y - 5, x, y + 5);
            canvas.fill(255);
            canvas.textSize(12);
            canvas.text("10^" + i, x, y + 20);
        }

        // Draw range highlights
        for (Range range : ranges) {
            float x1 = PApplet.map((float) Math.log10(range.start()), 0, (float) Math.log10(maxRangeEnd), 0, canvas.width);
            float x2 = PApplet.map((float) Math.log10(range.end()), 0, (float) Math.log10(maxRangeEnd), 0, canvas.width);
            canvas.fill(100, 100, 255, 100); // Blue tint for ranges
            canvas.rect(x1, y - 25, x2 - x1, 25);
        }
    }

    private void drawTotalSum(PApplet canvas) {
        canvas.fill(0, 255, 0);
        canvas.textSize(32);
        canvas.text("Total Sum: " + totalSum, canvas.width / 2f, canvas.height - 150);
    }

    private record Range(long start, long end) {
        private static Range parse(final String input) {
            final String[] parts = input.split("-");
            return new Range(Long.parseLong(parts[0]), Long.parseLong(parts[1]));
        }
    }

    private record RangePart1(Range range) {
        private long sumInvalidIds() {
            long totalSum = 0;

            // d is the number of digits in the first half
            for (int d = 1; d <= 8; d++) {
                final long powerOf10 = (long) Math.pow(10, d);
                final long combinator = powerOf10 + 1;

                // Smallest and largest possible first halves with 'd' digits
                final long minF = (long) Math.pow(10, d - 1);
                final long maxF = powerOf10 - 1;

                // Calculate the bounds for F based on the range [start, end]
                final long lowerBoundF = (range.start + combinator - 1) / combinator; // ceil division
                final long upperBoundF = range.end / combinator;

                // The actual range of F we need to sum
                final long actualMinF = Math.max(minF, lowerBoundF);
                final long actualMaxF = Math.min(maxF, upperBoundF);

                if (actualMinF > actualMaxF) {
                    continue;
                }

                // Use arithmetic series sum formula: n/2 * (first + last)
                final long numTerms = actualMaxF - actualMinF + 1;
                final long sumOfF = numTerms * (actualMinF + actualMaxF) / 2;
                totalSum += sumOfF * combinator;
            }

            return totalSum;
        }
    }

    private record RangePart2(Range range) {
        private long sumInvalidIds() {
            final Set<Long> invalidIds = new HashSet<>();
            final int endLen = String.valueOf(range.end).length();

            // Loop through possible lengths of the final number S
            for (int lenS = 2; lenS <= endLen; lenS++) {
                // Loop through possible lengths of the prefix P
                for (int lenP = 1; lenP <= lenS / 2; lenP++) {
                    if (lenS % lenP == 0) {
                        // Calculate the combinator using the geometric series formula
                        // S = P * (10^(lenS) - 1) / (10^(lenP) - 1)
                        final long powerOf10_S = (long) Math.pow(10, lenS);
                        final long powerOf10_P = (long) Math.pow(10, lenP);
                        final long combinator = (powerOf10_S - 1) / (powerOf10_P - 1);

                        // Smallest and largest possible prefixes P of length lenP
                        final long minP = (long) Math.pow(10, lenP - 1);
                        final long maxP = powerOf10_P - 1;

                        // Determine the range of prefixes P that generate numbers S within [start, end]
                        final long lowerBoundP = (range.start + combinator - 1) / combinator;
                        final long upperBoundP = range.end / combinator;

                        // Find the intersection of the possible prefixes and the required prefixes
                        final long actualMinP = Math.max(minP, lowerBoundP);
                        final long actualMaxP = Math.min(maxP, upperBoundP);

                        // Iterate through the small, valid range of prefixes and generate the invalid numbers
                        for (long p = actualMinP; p <= actualMaxP; p++) {
                            invalidIds.add(p * combinator);
                        }
                    }
                }
            }

            return invalidIds.stream()
                    .mapToLong(Long::longValue)
                    .sum();
        }
    }
}
