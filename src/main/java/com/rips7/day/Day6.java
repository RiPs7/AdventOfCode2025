package com.rips7.day;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.stream.IntStream;

public class Day6 implements Day<Long> {

    @Override
    public Long part1(String input) {
        final List<Problem> problems = parseProblems(input, false);
        return problems.stream()
                .mapToLong(Problem::solve)
                .sum();
    }

    @Override
    public Long part2(String input) {
        final List<Problem> problems = parseProblems(input, true);
        return problems.stream()
                .mapToLong(Problem::solve)
                .sum();
    }

    private List<Problem> parseProblems(final String input, final boolean rightToLeft) {
        final String[] lines = input.lines().toArray(String[]::new);
        final String[] lineNumbers = Arrays.stream(lines, 0, lines.length - 1).toArray(String[]::new);
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
        final List<List<Long>> numbers = new ArrayList<>();

        final int minLength = Arrays.stream(lines)
                .mapToInt(String::length)
                .min()
                .orElseThrow();
        final List<Integer> problemIndices = IntStream.range(0, minLength)
                .filter(i -> Arrays.stream(lines)
                        .map(line -> line.charAt(i))
                        .allMatch(c -> c == ' '))
                .boxed()
                .toList();

        final int maxLength = Arrays.stream(lines)
                .mapToInt(String::length)
                .max()
                .orElseThrow();

        List<Long> problemNumbers = new ArrayList<>();

        for (int i = maxLength - 1; i >= 0; i--) {
            if (problemIndices.contains(i)) {
                numbers.add(problemNumbers);
                problemNumbers = new ArrayList<>();
                continue;
            }
            long n = 0;
            for (String line : lines) {
                if (line.length() <= i || line.charAt(i) == ' ') {
                    continue;
                }
                n = 10 * n + Integer.parseInt(String.valueOf(line.charAt(i)));
            }
            problemNumbers.add(n);
        }
        numbers.add(problemNumbers);

        return IntStream.range(0, numbers.size())
                .map(i -> numbers.size() - 1- i)
                .mapToObj(numbers::get)
                .toList();
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
