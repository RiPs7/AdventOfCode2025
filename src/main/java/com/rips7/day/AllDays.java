package com.rips7.day;

import java.lang.reflect.InvocationTargetException;
import java.util.stream.Stream;

public class AllDays {

    public static Stream<Day<?>> getAllDays() {
        return Stream.of(Day1.class)
                .map(clazz -> {
                    try {
                        return clazz.getConstructor().newInstance();
                    } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}