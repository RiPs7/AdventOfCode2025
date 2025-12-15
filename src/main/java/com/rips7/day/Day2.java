package com.rips7.day;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Day2 implements Day<Long> {

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
