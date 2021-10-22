package it.areson.aresonsomnium.elements;

public record Multiplier(double value, String expiry) {

    public Multiplier() {
        this(1.0, "Permanente");
    }

}
