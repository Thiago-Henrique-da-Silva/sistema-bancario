package com.thiago.sistema_bancario.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record DepositOrWithdrawalRequest(
        @NotNull(message = "Valor invalido.")
        @Positive(message = "Valor deve ser positivo.")
        BigDecimal value) {
}
