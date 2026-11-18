package com.thiago.sistema_bancario.repository;

import com.thiago.sistema_bancario.domain.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long> {

    boolean existsByCpfOrEmail(String cpf, String email);
}
