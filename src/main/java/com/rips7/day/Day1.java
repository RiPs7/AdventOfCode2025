package com.rips7.day;

import processing.core.PApplet;

public class Day1 implements Day<Long> {

    private float part1CircleSize;
    private float part2SquareSize;

    @Override
    public void setupVisuals(PApplet canvas, String input) {
        part1CircleSize = 50;
        part2SquareSize = 300;
        canvas.textAlign(PApplet.CENTER, PApplet.CENTER);
        canvas.textSize(32);
    }

    @Override
    public boolean drawPart1(PApplet canvas) {
        canvas.background(0);
        canvas.fill(255);
        canvas.text("Part 1", (float) canvas.width / 2, 50);

        canvas.noFill();
        canvas.stroke(255, 0, 0);
        canvas.ellipse((float) canvas.width / 2, (float) canvas.height / 2, part1CircleSize, part1CircleSize);
        part1CircleSize += 2;

        return part1CircleSize < 400; // Continue animation until the circle is big enough
    }

    @Override
    public boolean drawPart2(PApplet canvas) {
        canvas.background(0);
        canvas.fill(255);
        canvas.text("Part 2", (float) canvas.width / 2, 50);

        canvas.noFill();
        canvas.stroke(0, 255, 0);
        canvas.rectMode(PApplet.CENTER);
        canvas.square((float) canvas.width / 2, (float) canvas.height / 2, part2SquareSize);
        part2SquareSize -= 2;

        return part2SquareSize > 0; // Continue animation until the square disappears
    }
}
