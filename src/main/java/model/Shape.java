package model;

import java.util.Random;

public enum Shape {
    Circle,
    Triangle,
    Square,
    Star,
    Diamond,
    Cross,
    Pentagon,
    Hexagon;

    private static final Random RANDOM = new Random();

    public static Shape getRandomShape() {
        Shape[] shapes = values();
        return shapes[RANDOM.nextInt(shapes.length)];
    }
}
