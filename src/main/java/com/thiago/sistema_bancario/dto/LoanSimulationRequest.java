package com.thiago.sistema_bancario.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record LoanSimulationRequest(
        @NotNull(message = "valor solicitado obrigatório.")
        @Positive(message = "valor solicitado deve ser positivo.")
        BigDecimal requestAmount,
        @NotNull(message = "parcelas obrigatória.")
        @Positive(message = "valor inserido deve ser positivo.")
        @Min(value = 1, message = "minimo de parcelas aceitas é 1.")
        @Max(value = 24, message = "maximo de parcelas aceitas e 24.")
        int numberInstallments
) {
}
