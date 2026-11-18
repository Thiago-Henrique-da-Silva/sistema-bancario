package com.thiago.sistema_bancario.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record LoanRequest(
        @NotNull(message = "valor solicitado obrigatório.")
        @Positive(message = "valor solicitado deve ser positivo.")
        BigDecimal requestAmount,
        @NotNull(message = "parcelas obrigatória.")
        @Positive(message = "valor inserido deve ser positivo.")
        @Min(value = 1, message = "mínimo de parcelas aceitas é 1.")
        @Max(value = 24, message = "máximo de parcelas aceitas e 24.")
        int numberInstallments){
}
