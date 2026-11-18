package com.thiago.sistema_bancario.repository;

import com.thiago.sistema_bancario.domain.Account;
import com.thiago.sistema_bancario.domain.Extract;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExtractRepository extends JpaRepository<Extract,Long> {

    Page<Extract> findByAccountOrderByDateTimeDesc(Account acoount, Pageable pageable);
}
