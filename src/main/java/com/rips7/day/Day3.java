package com.rips7.day;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Day3 implements Day<Long> {

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
