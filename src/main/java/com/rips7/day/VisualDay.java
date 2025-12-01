package com.rips7.day;

import processing.core.PApplet;

public class VisualDay<T> extends PApplet {

    private enum State {
        PART1,
        PART2,
        DONE
    }

    private final Day<T> day;
    private final String input;
    private State currentState;

    public VisualDay(final Day<T> day) {
        this.day = day;
        this.input = day.loadInput();
        this.currentState = State.PART1;
    }

    @Override
    public void settings() {
        size(800, 600);
    }

    @Override
    public void setup() {
        day.setupVisuals(this, input);
    }

    @Override
    public void draw() {
        switch (currentState) {
            case PART1:
                if (!day.drawPart1(this)) {
                    currentState = State.PART2;
                }
                break;
            case PART2:
                if (!day.drawPart2(this)) {
                    currentState = State.DONE;
                }
                break;
            case DONE:
                // You could add a "Finished!" message here
                // noLoop(); // Optional: stop the draw loop
                break;
        }
    }
}
