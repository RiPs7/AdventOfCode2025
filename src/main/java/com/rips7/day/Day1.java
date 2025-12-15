package com.rips7.day;

public class Day1 implements Day<Integer> {


    @Override
    public Integer part1(String input) {
        final Dial dial = new Dial(0, 99, 50);
        return (int) input.lines()
            .map(Rotation::from)
            .filter(dial::rotateSimple)
            .count();
    }

    @Override
    public Integer part2(String input) {
        final Dial dial = new Dial(0, 99, 50);
        return input.lines()
            .map(Rotation::from)
            .mapToInt(dial::rotateClick)
            .sum();
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
