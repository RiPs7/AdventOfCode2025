package com.rips7.day;

import com.rips7.util.Util.Position;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

public class Day12 implements Day<Long> {

    @Override
    public Long part1(String input) {
        final ChristmasTreeFarm farm = ChristmasTreeFarm.parse(input);
        return farm.regions.stream()
          .filter(region -> region.fits(farm.presents))
          .count();
    }


    @Override
    public Long part2(String input) {
        return 0L;
    }

    private record ChristmasTreeFarm(List<Present> presents, List<Region> regions) {
        private static ChristmasTreeFarm parse(final String input) {
            final String[] blocks = input.split("\n\n");
            final List<Present> presents = IntStream.range(0, blocks.length - 1)
              .mapToObj(i -> Present.parse(blocks[i]))
              .toList();
            final List<Region> regions = blocks[blocks.length - 1].lines()
              .map(Region::parse)
              .toList();
            return new ChristmasTreeFarm(presents, regions);
        }
    }

    private record Present(int id, Set<Position> points) {
        private static Present parse(final String input) {
            final String[] lines = input.split("\n");
            final int id = Integer.parseInt(lines[0].replace(":", ""));
            final Set<Position> points = new HashSet<>();
            for (int row = 1; row < lines.length; row++) {
                final String line = lines[row];
                for (int col = 0; col < line.length(); col++) {
                    if (line.charAt(col) == '#') {
                        points.add(Position.of(row, col));
                    }
                }
            }
            return new Present(id, points);
        }

        private int size() {
            return points.size();
        }
    }

    private record Region(int width, int height, List<Integer> presentCounts) {
        private static Region parse(final String input) {
            final String[] parts = input.split(": ");
            final String[] dimensions = parts[0].split("x");
            final int width = Integer.parseInt(dimensions[0]);
            final int height = Integer.parseInt(dimensions[1]);
            final List<Integer> presentCounts = Arrays.stream(parts[1].split(" "))
              .map(Integer::parseInt)
              .toList();
            return new Region(width, height, presentCounts);
        }

        private int area() {
            return width * height;
        }

        private boolean fits(final List<Present> presents) {
            // This is a naive approach to only check if the total present size does not exceed the area.
            // This problem does not require manipulating geometry.
            final int totalPresentsSize = IntStream.range(0, presentCounts.size())
              .map(i -> presentCounts.get(i) * presents.get(i).size())
              .sum();
            return totalPresentsSize <= area();
        }
    }
}