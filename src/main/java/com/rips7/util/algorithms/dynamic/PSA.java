package com.rips7.util.algorithms.dynamic;

import com.rips7.util.Util;
import com.rips7.util.Util.Position;

import java.util.function.BinaryOperator;
import java.util.function.Function;

public record PSA<T>(T psa) {
    public static <T extends Number> PSA<T[][]> newPSA2D(
            final T[][] array, final Class<T> clazz, final T initializer, final Function<Position, T> getter,
            final BinaryOperator<T> add, final BinaryOperator<T> subtract
    ) {
        final T[][] psa = Util.newGeneric2DArray(clazz, array.length, array[0].length);
        Util.loop2D(psa, (e, i, j) -> psa[i][j] = initializer);
        Util.loop2D(psa, (e, i, j) -> {
            final T left = i > 0 ? psa[i - 1][j] : initializer;
            final T top = j > 0 ? psa[i][j - 1] : initializer;
            final T topLeft = i > 0 && j > 0 ? psa[i - 1][j - 1] : initializer;
            psa[i][j] = add.apply(subtract.apply(add.apply(left, top), topLeft), getter.apply(Position.of(i, j)));
        });
        return new PSA<>(psa);
    }
}
