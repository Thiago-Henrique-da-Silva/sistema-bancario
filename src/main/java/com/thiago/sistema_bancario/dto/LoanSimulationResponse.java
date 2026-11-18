package com.thiago.sistema_bancario.dto;

import java.math.BigDecimal;

public record LoanSimulationResponse(
        BigDecimal requestAmount,
        BigDecimal interestRate,
        int numberInstallments,
        BigDecimal totalValue,
        BigDecimal installmentAmount) {
}
