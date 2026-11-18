package com.thiago.sistema_bancario.Mapper;

import com.thiago.sistema_bancario.domain.Loan;
import com.thiago.sistema_bancario.dto.LoanResponse;
import com.thiago.sistema_bancario.dto.LoanSimulationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LoanMapper {

    @Mapping(target = "numberAccount", source = "account.number")
    LoanResponse toLoanResponse(Loan loan);
    LoanSimulationResponse toLoanSimulationResponse(Loan loan);
}
