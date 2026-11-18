package com.thiago.sistema_bancario.exception;

public class InvalidInputExeption extends RuntimeException  {
    public InvalidInputExeption(String message) {
        super(message);
    }
}
