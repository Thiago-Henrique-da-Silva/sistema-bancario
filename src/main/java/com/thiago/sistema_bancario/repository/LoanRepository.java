package com.thiago.sistema_bancario.repository;

import com.thiago.sistema_bancario.domain.Account;
import com.thiago.sistema_bancario.domain.Loan;
import com.thiago.sistema_bancario.domain.StatusLoan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    boolean existsByAccountAndLoanStatus(Account account, StatusLoan statusLoan);
    Optional<Loan> findByAccountAndLoanStatus(Account account, StatusLoan statusLoan);
}
