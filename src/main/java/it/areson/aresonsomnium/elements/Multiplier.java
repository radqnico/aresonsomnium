package it.areson.aresonsomnium.elements;

public class Multiplier {

    private final double value;
    private final String expiry;

    public Multiplier(double value, String expiry) {
        this.value = value;
        this.expiry = expiry;
    }

    public Multiplier() {
        this.value = 1.0;
        this.expiry = "Permanente";
    }

    public double getValue() {
        return value;
    }

    public String getExpiry() {
        return expiry;
    }

}
