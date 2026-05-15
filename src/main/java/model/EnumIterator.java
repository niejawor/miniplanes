package model;

import java.util.function.Supplier;
import java.util.Random;
import java.util.stream.IntStream;

/*
By Jakub Stachniak on 15.03.2026
Class that takes an enum as parameter and an Enum.class as constructor parameter and returns an iterator over the elements of that enum. Use example in model.Line class
Provides an option to get a random Enum value through the randomValue() method
 */

public final class EnumIterator<T extends Enum<T>> implements Supplier<T> {
    private int counter =- 1;
    private final T[] enumVals;
    private final Random intStream = new Random();

    public EnumIterator(Class<T> enumClass) {
        if ((enumVals = enumClass.getEnumConstants()).length == 0)
            throw new IllegalArgumentException("Bezelementowy Enum");
    }

    public T randomValue(){
        return enumVals[intStream.nextInt(enumVals.length)];
    }

    @Override
    public T get(){
        return enumVals[counter = (counter+1)%enumVals.length];
    }
}
