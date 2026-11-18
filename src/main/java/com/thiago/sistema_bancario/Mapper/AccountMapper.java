package com.thiago.sistema_bancario.Mapper;

import com.thiago.sistema_bancario.domain.Account;
import com.thiago.sistema_bancario.dto.AccountResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    AccountResponse toAccountResponse(Account account);
}
