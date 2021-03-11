package elements;

public class Multiplier {

    private final double value;
    private final String expiry;

    private long eventNumber;

    public Multiplier(double value, String expiry) {
        this.value = value;
        this.expiry = expiry;
        this.eventNumber = 0;
    }

    public Multiplier() {
        this.value = 1.0;
        this.expiry = "Permanente";
        this.eventNumber = 0;
    }

    public double getValue() {
        return value;
    }

    public String getExpiry() {
        return expiry;
    }

    public long getEventNumber() {
        return eventNumber;
    }

    public void setEventNumber(long eventNumber) {
        this.eventNumber = eventNumber;
    }

}
