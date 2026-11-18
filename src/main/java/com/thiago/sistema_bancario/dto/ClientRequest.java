package com.thiago.sistema_bancario.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ClientRequest(
        @NotEmpty(message = "Nome obrigátorio.")
        String name,
        @NotEmpty(message = "Cpf obrigátorio.")
        @Pattern(regexp = "\\d{11}", message = "CPF deve conter 11 numeros.")
        String cpf,
        @NotEmpty(message = "Email obrigátorio.")
        @Email(message = "Email invalido.")
        String email,
        @NotNull(message = "Renda mensal obrigatória.")
        @Positive(message = "Valor deve ser positivo.")
        BigDecimal monthlyIncome) {
}
