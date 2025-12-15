package com.rips7.day;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Day6 implements Day<Long> {

    @Override
    public Long part1(String input) {
        return parseProblems(input, false).stream()
                .mapToLong(Problem::solve)
                .sum();
    }

    @Override
    public Long part2(String input) {
        return parseProblems(input, true).stream()
                .mapToLong(Problem::solve)
                .sum();
    }

    private List<Problem> parseProblems(final String input, final boolean rightToLeft) {
        final String[] lines = input.lines().toArray(String[]::new);
        final String[] lineNumbers = Arrays.copyOf(lines, lines.length - 1);
        final List<List<Long>> numbers = rightToLeft ?
                parseRightToLeft(lineNumbers) :
                parseLeftToRight(lineNumbers);
        final List<Character> operations = Arrays.stream(lines[lines.length - 1].split("\\s+"))
                .map(e -> e.charAt(0))
                .toList();
        return IntStream.range(0, numbers.size())
                .mapToObj(i -> new Problem(numbers.get(i), operations.get(i)))
                .toList();
    }

    private List<List<Long>> parseLeftToRight(final String[] lines) {
        final List<List<Long>> inlineNumbers = Arrays.stream(lines)
                .map(line -> Arrays.stream(line.trim().split("\\s+"))
                        .map(Long::parseLong)
                        .toList())
                .toList();
        return IntStream.range(0, inlineNumbers.getFirst().size())
                .mapToObj(col -> inlineNumbers.stream()
                        .map(row -> row.get(col))
                        .toList())
                .toList();
    }

    private List<List<Long>> parseRightToLeft(final String[] lines) {
        final int maxWidth = Arrays.stream(lines).mapToInt(String::length).max().orElse(0);
        final List<String> paddedLines = Arrays.stream(lines)
                .map(line -> String.format("%-" + maxWidth + "s", line))
                .toList();

        final List<String> columns = IntStream.range(0, maxWidth)
                .mapToObj(c -> paddedLines.stream()
                        .map(row -> String.valueOf(row.charAt(c)))
                        .collect(Collectors.joining()))
                .collect(Collectors.toList());

        Collections.reverse(columns);

        final String allProblemsString = String.join("|", columns);
        final List<List<Long>> problemNumbers = Arrays.stream(allProblemsString.split("\\|\\s+\\|"))
                .map(problemString -> Arrays.stream(problemString.split("\\|"))
                        .map(s -> s.replaceAll("\\s", ""))
                        .filter(s -> !s.isEmpty())
                        .map(Long::parseLong)
                        .toList())
                .collect(Collectors.toList());

        Collections.reverse(problemNumbers);

        return problemNumbers;
    }

    private record Problem(List<Long> numbers, BinaryOperator<Long> accumulator) {
        private Problem(final List<Long> numbers, final char operation) {
            this(numbers, getAccumulator(operation));
        }

        private static BinaryOperator<Long> getAccumulator(final char operation) {
            return switch (operation) {
                case '+' -> Long::sum;
                case '*' -> (a, b) -> a * b;
                default -> throw new RuntimeException();
            };
        }

        private Long solve() {
            return numbers.stream()
                    .reduce(accumulator)
                    .orElseThrow();
        }
    }
}
