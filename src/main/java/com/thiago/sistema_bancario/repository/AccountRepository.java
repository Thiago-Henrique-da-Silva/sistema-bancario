package com.thiago.sistema_bancario.repository;

import com.thiago.sistema_bancario.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long>{

    boolean existsByClientId(Long clientId);
    boolean existsByNumber(String number);
    Account findByNumber(String number);

}
