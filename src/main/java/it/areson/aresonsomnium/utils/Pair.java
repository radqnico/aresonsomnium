package it.areson.aresonsomnium.utils;

import java.time.Duration;

public class Pair<A, B> {

    private final A left;
    private final B right;

    private Pair(A left, B right) {
        this.left = left;
        this.right = right;
    }

    public static <A, B> Pair<A, B> of(A left, B right) {
        return new Pair<>(left, right);
    }

    public A left() {
        return left;
    }

    public B right() {
        return right;
    }

    public static Pair<Double, Duration> emptyMultiplier() {
        return new Pair<>(0.0, Duration.ZERO);
    }

    public static Pair<Double, Duration> compare(Pair<Double, Duration> a, Pair<Double, Duration> b) {
        return b;
    }

}
