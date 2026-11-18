package com.thiago.sistema_bancario.dto;

import com.thiago.sistema_bancario.domain.StatusLoan;
import java.math.BigDecimal;

public record LoanResponse(
        Long id,
        BigDecimal requestAmount,
        BigDecimal interestRate,
        int numberInstallments,
        BigDecimal totalValue,
        BigDecimal installmentAmount,
        int paidInstallments,
        StatusLoan loanStatus,
        String numberAccount) {
}
