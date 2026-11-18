package com.thiago.sistema_bancario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransferRequest(
        @NotBlank(message = "numero da conta de origem invalida.")
        @Pattern(regexp = "\\d{6}", message = "numero da conta deve conter 6 digitos.")
        String originAccountNumber,
        @NotBlank(message = "numero da conta destinatário invalida.")
        @Pattern(regexp = "\\d{6}", message = "numero da conta deve conter 6 digitos.")
        String destinationAccountNumber,
        @NotNull(message = "valor invalido.")
        @Positive(message = "valor deve ser positivo.")
        BigDecimal value
) {
}
