package com.rips7.day;

import com.rips7.util.Util;
import processing.core.PApplet;

import static com.rips7.util.Util.TimedResult;
import static com.rips7.util.Util.printColor;
import static com.rips7.util.Util.time;

public interface Day<T> {
    boolean VISUALIZE = true;

    default T part1(@SuppressWarnings("unused") String input) {
        // Do nothing by default
        return null;
    }

    default T part2(@SuppressWarnings("unused") String input) {
        // Do nothing by default
        return null;
    }

    default void setupVisuals(@SuppressWarnings("unused") PApplet canvas, @SuppressWarnings("unused") String input) {
        // Do nothing by default
    }

    default boolean drawPart1(@SuppressWarnings("unused") PApplet canvas) {
        part1(loadInput());
        return false;
    }

    default boolean drawPart2(@SuppressWarnings("unused") PApplet canvas) {
        part2(loadInput());
        return false;
    }

    default void run() {
        if (VISUALIZE) {
            PApplet.runSketch(new String[]{ VisualDay.class.getName() }, new VisualDay<>(this));
        } else {
            final String input = loadInput();

            System.out.printf("----- %s -----%n", getClass().getSimpleName());

            final TimedResult<T> part1Res = time(() -> part1(input));
            System.out.print("Part 1: ");
            printColor("%s ".formatted(part1Res.res()), Util.AnsiColor.GREEN);
            printColor("(%s)%n".formatted(part1Res.timeInfo()), Util.AnsiColor.YELLOW);

            final TimedResult<T> part2Res = time(() -> part2(input));
            System.out.print("Part 2: ");
            printColor("%s ".formatted(part2Res.res()), Util.AnsiColor.GREEN);
            printColor("(%s)%n".formatted(part2Res.timeInfo()), Util.AnsiColor.YELLOW);

            System.out.println("----------------");
        }
    }

    default String loadInput() {
        final String inputFilename = "/%s/input".formatted(this.getClass().getSimpleName().toLowerCase());
        try {
            return Util.readResource(inputFilename);
        } catch (final NullPointerException e) {
            throw new RuntimeException("No input file '%s'".formatted(inputFilename));
        }
    }

}