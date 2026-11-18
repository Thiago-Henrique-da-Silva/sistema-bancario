package com.thiago.sistema_bancario.dto;

import java.math.BigDecimal;

public record ClientResponse(
        Long id,
        String name,
        String cpf,
        String email,
        BigDecimal monthlyIncome){
}
