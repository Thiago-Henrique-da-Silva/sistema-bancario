package com.thiago.sistema_bancario.dto;

import com.thiago.sistema_bancario.domain.StatusAccount;
import java.math.BigDecimal;

public record AccountResponse(
        Long id,
        String number,
        BigDecimal balance,
        BigDecimal overdraftLimit,
        BigDecimal  overdraftUsed,
        StatusAccount status,
        ClientResponse client,
        BigDecimal overdraftAvailable,
        boolean usingOverdraft){
}
