package com.rips7.day;

import processing.core.PApplet;

import java.util.List;

public class Day1 implements Day<Integer> {

    private static final int ANIMATION_DURATION = 30;

    private Dial dial;
    private List<Rotation> rotations;
    private int totalRotations;

    private int rotationIndex;
    private int rotationStartValue;
    private int animationFrame;
    private int part1Password;
    private int part2Password;
    private boolean isPart2Setup = false;
    private int flashCountdown = 0;
    private float previousUnwrappedAngle;


    @Override
    public Integer part1(String input) {
        dial = new Dial(0, 99, 50);
        return (int) input.lines()
            .map(Rotation::from)
            .filter(dial::rotateSimple)
            .count();
    }

    @Override
    public Integer part2(String input) {
        dial = new Dial(0, 99, 50);
        return input.lines()
            .map(Rotation::from)
            .mapToInt(dial::rotateClick)
            .sum();
    }

    @Override
    public boolean visualize() {
        return Day.super.visualize();
//         return true;
    }

    @Override
    public void setupVisuals(PApplet canvas, String input) {
        this.rotations = input.lines().map(Rotation::from).toList();
        this.totalRotations = this.rotations.size();
        this.dial = new Dial(0, 99, 50);
        this.rotationIndex = 0;
        this.rotationStartValue = dial.value;
        this.part1Password = 0;
        this.part2Password = 0;
        this.animationFrame = 0;
        canvas.textAlign(PApplet.CENTER, PApplet.CENTER);
    }

    @Override
    public boolean drawPart1(PApplet canvas) {
        if (rotationIndex >= rotations.size()) {
            return false;
        }

        if (animationFrame == 0) {
            rotationStartValue = dial.value;
        }

        animationFrame++;
        float progress = (float) animationFrame / ANIMATION_DURATION;

        if (progress >= 1.0f) {
            animationFrame = 0;
            if (dial.rotateSimple(rotations.get(rotationIndex))) {
                part1Password++;
            }
            rotationIndex++;
            progress = 0;
        }

        drawBackground(canvas, "Part 1");
        drawDial(canvas);
        drawPassword(canvas, part1Password);
        drawRotationInfo(canvas);
        drawProgress(canvas);
        if (rotationIndex < rotations.size()) {
            drawPointerAnimation(canvas, rotationStartValue, rotations.get(rotationIndex), progress, false);
        }

        return true;
    }

    @Override
    public boolean drawPart2(PApplet canvas) {
        if (!isPart2Setup) {
            this.dial = new Dial(0, 99, 50);
            this.rotationIndex = 0;
            this.animationFrame = 0;
            isPart2Setup = true;
        }

        if (rotationIndex >= rotations.size()) {
            return false;
        }

        if (animationFrame == 0) {
            rotationStartValue = dial.value;
            previousUnwrappedAngle = PApplet.map(rotationStartValue, 0, 100, 0, PApplet.TWO_PI) - PApplet.HALF_PI;
        }

        animationFrame++;
        float progress = (float) animationFrame / ANIMATION_DURATION;

        if (progress >= 1.0f) {
            animationFrame = 0;
            int clicks = dial.rotateClick(rotations.get(rotationIndex));
            part2Password += clicks;
            if (dial.value == 0) {
                flashCountdown = 15;
            }
            rotationIndex++;
            progress = 0;
        }

        drawBackground(canvas, "Part 2");
        if (flashCountdown > 0) {
            canvas.background(100, 0, 0);
            flashCountdown--;
        }
        drawDial(canvas);
        drawPassword(canvas, part2Password);
        drawRotationInfo(canvas);
        drawProgress(canvas);
        if (rotationIndex < rotations.size()) {
            drawPointerAnimation(canvas, rotationStartValue, rotations.get(rotationIndex), progress, true);
        }

        return true;
    }

    private void drawBackground(PApplet canvas, String title) {
        canvas.background(0);
        canvas.fill(255);
        canvas.textSize(32);
        canvas.text(title, (float) canvas.width / 2, 50);
    }

    private void drawDial(PApplet canvas) {
        float centerX = canvas.width / 2f;
        float centerY = canvas.height / 2f;
        float diameter = 380;
        float textRadius = diameter / 2f + 30;

        canvas.noFill();
        canvas.stroke(255);
        canvas.ellipse(centerX, centerY, diameter, diameter);

        canvas.fill(255);
        canvas.textSize(20);
        for (int i = 0; i < 100; i += 10) {
            float angle = PApplet.map(i, 0, 100, 0, PApplet.TWO_PI) - PApplet.HALF_PI;
            float textX = centerX + PApplet.cos(angle) * textRadius;
            float textY = centerY + PApplet.sin(angle) * textRadius;
            canvas.text(i, textX, textY);
        }
    }

    private void drawPointerAnimation(PApplet canvas, int startValue, Rotation rotation, float progress, boolean checkClicks) {
        float centerX = canvas.width / 2f;
        float centerY = canvas.height / 2f;
        float pointerLength = 380 / 2f;

        float startAngle = PApplet.map(startValue, 0, 100, 0, PApplet.TWO_PI) - PApplet.HALF_PI;
        int endValue = dial.peekValue(rotation);
        float endAngle = PApplet.map(endValue, 0, 100, 0, PApplet.TWO_PI) - PApplet.HALF_PI;

        canvas.strokeWeight(2);
        canvas.stroke(255, 0, 0, 150);
        canvas.line(centerX, centerY, centerX + PApplet.cos(startAngle) * pointerLength, centerY + PApplet.sin(startAngle) * pointerLength);
        canvas.stroke(0, 255, 0, 150);
        canvas.line(centerX, centerY, centerX + PApplet.cos(endAngle) * pointerLength, centerY + PApplet.sin(endAngle) * pointerLength);

        float totalRotationAngle = PApplet.TWO_PI * (rotation.value() / 100.0f);
        if (rotation.dir() == Direction.LEFT) {
            totalRotationAngle = -totalRotationAngle;
        }
        float currentUnwrappedAngle = startAngle + (totalRotationAngle * progress);

        if (checkClicks && animationFrame > 1) {
            float zeroAngleBoundary = -PApplet.HALF_PI;
            long prevCrossings = (long) Math.floor((previousUnwrappedAngle - zeroAngleBoundary) / PApplet.TWO_PI);
            long currentCrossings = (long) Math.floor((currentUnwrappedAngle - zeroAngleBoundary) / PApplet.TWO_PI);
            if (currentCrossings != prevCrossings) {
                flashCountdown = 15;
            }
        }
        previousUnwrappedAngle = currentUnwrappedAngle;

        canvas.stroke(255);
        canvas.strokeWeight(3);
        canvas.line(centerX, centerY, centerX + PApplet.cos(currentUnwrappedAngle) * pointerLength, centerY + PApplet.sin(currentUnwrappedAngle) * pointerLength);
        canvas.strokeWeight(1);
    }

    private void drawPassword(PApplet canvas, int password) {
        canvas.fill(0, 255, 0);
        canvas.textSize(48);
        canvas.text("Password: " + password, (float) canvas.width / 2, canvas.height - 30);
    }

    private void drawRotationInfo(PApplet canvas) {
        canvas.textSize(24);
        canvas.textAlign(PApplet.LEFT, PApplet.TOP);

        if (rotationIndex < rotations.size()) {
            Rotation current = rotations.get(rotationIndex);
            canvas.fill(255, 255, 0);
            canvas.text("Current: " + current, 20, 80);
        }

        if (rotationIndex > 0) {
            Rotation previous = rotations.get(rotationIndex - 1);
            canvas.fill(180);
            canvas.text("Previous: " + previous, 20, 110);
        }

        canvas.textAlign(PApplet.CENTER, PApplet.CENTER);
    }

    private void drawProgress(PApplet canvas) {
        canvas.fill(255);
        canvas.textSize(24);
        canvas.textAlign(PApplet.RIGHT, PApplet.TOP);
        float currentProgress = (float) rotationIndex / (totalRotations * 2) * 100;
        canvas.text(String.format("Progress: %.1f%%", currentProgress), canvas.width - 20, 80);
        canvas.textAlign(PApplet.CENTER, PApplet.CENTER);
    }

    private enum Direction {
        LEFT, RIGHT
    }

    private record Rotation(Direction dir, int value) {
        @Override
        public String toString() {
            return (dir == Direction.LEFT ? "L" : "R") + value;
        }

        private static Rotation from(final String input) {
            final Direction dir = switch (input.charAt(0)) {
                case 'L' -> Direction.LEFT;
                case 'R' -> Direction.RIGHT;
                default -> throw new IllegalArgumentException("Unknown rotation for " + input);
            };
            final int value = Integer.parseInt(input.substring(1));
            return new Rotation(dir, value);
        }
    }

    private static final class Dial {
        final int start;
        final int end;
        final int range;
        int value;

        private Dial(final int start, final int end, final int value) {
            this.start = start;
            this.end = end;
            this.range = end - start + 1;
            this.value = value;
        }

        public int peekValue(final Rotation rotation) {
            final int delta = switch (rotation.dir) {
                case LEFT -> -rotation.value;
                case RIGHT -> rotation.value;
            };
            return Math.floorMod(this.value + delta, this.range);
        }

        private boolean rotateSimple(final Rotation rotation) {
            this.value = peekValue(rotation);
            return this.value == 0;
        }

        private int rotateClick(final Rotation rotation) {
            int clicks = 0;
            final int startPos = this.value;
            final int endPos = switch (rotation.dir) {
                case LEFT -> startPos - rotation.value;
                case RIGHT -> startPos + rotation.value;
            };

            if (rotation.dir == Direction.RIGHT) {
                clicks += Math.floorDiv(endPos - 1, range) - Math.floorDiv(startPos, range);
            } else {
                clicks += Math.floorDiv(startPos - 1, range) - Math.floorDiv(endPos, range);
            }

            this.value = Math.floorMod(endPos, range);

            if (this.value == 0) {
                clicks++;
            }

            return clicks;
        }
    }
}
