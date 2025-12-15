package com.rips7.day;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;

public class Day5 implements Day<Long> {

    @Override
    public Long part1(String input) {
        final String[] sections = input.split("\\R\\R", 2);

        final List<Range> flattenedRanges = sections[0].lines()
                .map(Range::parse)
                // Sort and merge overlapping ranges to avoid unnecessary comparisons
                .sorted(Comparator.comparingLong(Range::startId))
                .collect(ArrayList::new, Range.rangeMerger(), ArrayList::addAll);

        return sections[1].lines()
                .map(Long::parseLong)
                .filter(id -> flattenedRanges.stream().anyMatch(range -> range.contains(id)))
                .count();
    }

    @Override
    public Long part2(String input) {
        final String[] sections = input.split("\\R\\R", 2);

        final List<Range> flattenedRanges = sections[0].lines()
                .map(Range::parse)
                // Sort and merge overlapping ranges to avoid unnecessary comparisons
                .sorted(Comparator.comparingLong(Range::startId))
                .collect(ArrayList::new, Range.rangeMerger(), ArrayList::addAll);

        return flattenedRanges.stream()
                .mapToLong(Range::numIds)
                .sum();
    }

    private record Range(long startId, long endId) {
        private static Range parse(final String input) {
            final String[] parts = input.split("-");
            return new Range(Long.parseLong(parts[0]), Long.parseLong(parts[1]));
        }

        private static BiConsumer<ArrayList<Range>, Range> rangeMerger() {
            return (list, range) -> {
                if (list.isEmpty() || list.getLast().isOutside(range)) {
                    list.add(range);
                } else {
                    list.set(list.size() - 1, list.getLast().merge(range));
                }
            };
        }

        private boolean isOutside(final Range other) {
            return this.startId > other.endId || other.startId > this.endId;
        }

        private boolean contains(final long id) {
            return this.startId <= id && id <= this.endId;
        }

        private Range merge(Range other) {
            return new Range(Math.min(this.startId, other.startId), Math.max(this.endId, other.endId));
        }

        private long numIds() {
            return this.endId - this.startId + 1;
        }
    }
}
