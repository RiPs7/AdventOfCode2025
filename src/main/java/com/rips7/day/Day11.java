package com.rips7.day;

import com.rips7.util.algorithms.graphs.Graph;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Day11 implements Day<Long> {

    @Override
    public Long part1(String input) {
        final Graph<String> graph = parseGraph(input);
        return graph.countPaths("you", "out");
    }

    @Override
    public Long part2(String input) {
        final Graph<String> graph = parseGraph(input);

        long allPaths = 0;
        final long paths1 = graph.countPaths("dac", "fft");
        if (paths1 > 0) {
            final long leftPaths = graph.countPaths("svr", "dac");
            final long rightPaths = graph.countPaths("fft", "out");
            allPaths += paths1 * leftPaths * rightPaths;
        }
        final long paths2 = graph.countPaths("fft", "dac");
        if (paths2 > 0) {
            final long leftPaths = graph.countPaths("svr", "fft");
            final long rightPaths = graph.countPaths("dac", "out");
            allPaths += paths2 * leftPaths * rightPaths;
        }
        return allPaths;
    }

    private static Graph<String> parseGraph(final String input) {
        final Map<String, Set<String>> connections = input.lines()
                .map(line -> line.split(": "))
                .collect(Collectors.toMap(
                        parts -> parts[0],
                        parts -> Arrays.stream(parts[1].split(" ")).collect(Collectors.toSet())));
        final Set<String> nodes = Stream.concat(
                connections.keySet().stream(),
                connections.values().stream().flatMap(Set::stream))
                .collect(Collectors.toSet());
        return new Graph<>(nodes, connections);
    }

}