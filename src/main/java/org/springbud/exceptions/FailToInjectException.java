package org.springbud.exceptions;

public class FailToInjectException extends RuntimeException{
    public FailToInjectException(String message) {
        super(message);
    }
}
