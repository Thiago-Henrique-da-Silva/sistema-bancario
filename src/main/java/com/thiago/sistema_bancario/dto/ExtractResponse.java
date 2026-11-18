package com.thiago.sistema_bancario.dto;

import com.thiago.sistema_bancario.domain.TypeExtract;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ExtractResponse(
        Long id,
        String numberAccount,
        TypeExtract typeExtract,
        BigDecimal value,
        LocalDateTime dateTime) {
}
