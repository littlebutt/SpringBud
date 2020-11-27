package org.springbud.exceptions;

public class DatabaseProcessException extends RuntimeException{
    public DatabaseProcessException(String message) {
        super(message);
    }
}
