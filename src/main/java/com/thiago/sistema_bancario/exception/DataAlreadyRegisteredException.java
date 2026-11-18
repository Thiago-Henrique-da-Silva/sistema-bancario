package com.thiago.sistema_bancario.exception;

public class DataAlreadyRegisteredException extends RuntimeException {
    public DataAlreadyRegisteredException(String message) {
        super(message);
    }
}
