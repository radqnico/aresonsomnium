package it.areson.aresonsomnium.elements;

import java.time.Duration;

public record Pair<A, B>(A left, B right) {

    public static <A, B> Pair<A, B> of(A left, B right) {
        return new Pair<>(left, right);
    }

    public static Pair<Double, Duration> compare(Pair<Double, Duration> a, Pair<Double, Duration> b) {
        return b;
    }

    @Override
    public String toString() {
        return "Left: " + left + ", Right: " + right;
    }

}
