package com.rips7.day;

import com.rips7.util.algorithms.pathfinding.BFS;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.optimisation.Variable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class Day10 implements Day<Long> {

    @Override
    public Long part1(String input) {
        return input.lines()
                .map(Machine::parse)
                .mapToLong(Machine::fixIndicators)
                .sum();
    }

    @Override
    public Long part2(String input) {
        return input.lines()
                .map(Machine::parse)
                .mapToLong(Machine::fixJoltage)
                .sum();
    }

    private record IndicatorState(List<Boolean> indicators) {
        private static IndicatorState parse(final String input) {
            return new IndicatorState(input.chars()
                    .mapToObj(c -> c == '#')
                    .toList());
        }

        private static IndicatorState startState(final int n) {
            return new IndicatorState(IntStream.range(0, n)
                    .mapToObj(i -> false)
                    .toList());
        }

        private IndicatorState toggle(final List<Integer> indices) {
            return new IndicatorState(IntStream.range(0, indicators.size())
                    .mapToObj(i -> indices.contains(i) != indicators.get(i))
                    .toList());
        }
    }

    private record JoltageState(List<Integer> joltage) {
        private static JoltageState parse(final String input) {
            final List<Integer> joltage = Arrays.stream(input.split(","))
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .toList();
            return new JoltageState(joltage);
        }
    }

    private record Machine(List<List<Integer>> buttons, IndicatorState targetIndicatorState, JoltageState targetJoltageState) {
        private static final Pattern BUTTONS_PATTERN = Pattern.compile("\\((.*?)\\)");
        private static final Pattern INDICATOR_PATTERN = Pattern.compile("\\[(.*?)]");
        private static final Pattern JOLTAGE_PATTERN = Pattern.compile("\\{(.*?)}");

        private static Machine parse(final String input) {
            final List<List<Integer>> buttons = new ArrayList<>();
            final Matcher buttonsMatcher = BUTTONS_PATTERN.matcher(input);
            while (buttonsMatcher.find()) {
                final List<Integer> buttonIndicators = Arrays.stream(buttonsMatcher.group(1).split(","))
                        .map(String::trim)
                        .map(Integer::parseInt)
                        .toList();
                buttons.add(buttonIndicators);
            }

            final Matcher indicatorMatcher = INDICATOR_PATTERN.matcher(input);
            if (!indicatorMatcher.find()) {
                throw new IllegalArgumentException("Invalid indicator state format: " + input);
            }
            final IndicatorState targetIndicatorState = IndicatorState.parse(indicatorMatcher.group(1));

            final Matcher joltageMatcher = JOLTAGE_PATTERN.matcher(input);
            if (!joltageMatcher.find()) {
                throw new IllegalArgumentException("Invalid joltage state format: " + input);
            }
            final JoltageState targetJoltageState = JoltageState.parse(joltageMatcher.group(1));

            return new Machine(buttons, targetIndicatorState, targetJoltageState);
        }

        private long fixIndicators() {
            final List<IndicatorState> path = new BFS<IndicatorState>().run(
                    IndicatorState.startState(targetIndicatorState.indicators().size()),
                    targetIndicatorState,
                    indicatorState -> buttons.stream()
                            .map(indicatorState::toggle)
                            .toList());
            return path.size() - 1;
        }

        private long fixJoltage() {
            final ExpressionsBasedModel model = new ExpressionsBasedModel();
            final Variable[] vars = IntStream.range(0, buttons.size())
                    .mapToObj(i -> model.addVariable("x" + i)
                            .integer(true)
                            .lower(0.0)
                            .weight(1.0))
                    .toArray(Variable[]::new);

            for (int i = 0; i < targetJoltageState.joltage().size(); i++) {
                final Expression expr = model.addExpression("part_" + i)
                        .level(targetJoltageState.joltage().get(i));
                for (int j = 0; j < buttons.size(); j++) {
                    if (buttons.get(j).contains(i)) {
                        expr.set(vars[j], 1.0);
                    }
                }
            }

            final Result result = model.minimise();

            if (!result.getState().isFeasible()) {
                throw new IllegalStateException("No solution found for joltage");
            }

            return IntStream.range(0, vars.length)
                    .mapToLong(i -> Math.round(result.doubleValue(i)))
                    .sum();
        }
    }
}