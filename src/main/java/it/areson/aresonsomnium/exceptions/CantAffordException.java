package it.areson.aresonsomnium.exceptions;

public class CantAffordException extends RuntimeException {
    public CantAffordException(String message) {
        super(message);
    }
}
