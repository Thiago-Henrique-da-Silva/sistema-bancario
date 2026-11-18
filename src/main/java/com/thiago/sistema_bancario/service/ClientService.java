package com.thiago.sistema_bancario.service;

import com.thiago.sistema_bancario.Mapper.ClientMapper;

import com.thiago.sistema_bancario.domain.Client;
import com.thiago.sistema_bancario.dto.ClientRequest;
import com.thiago.sistema_bancario.dto.ClientResponse;
import com.thiago.sistema_bancario.exception.DataAlreadyRegisteredException;
import com.thiago.sistema_bancario.exception.ResourceNotFoundException;
import com.thiago.sistema_bancario.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClientService {
    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    @Transactional
    public ClientResponse registerClient(ClientRequest clientRequest) {
        if (clientRepository.existsByCpfOrEmail(clientRequest.cpf(), clientRequest.email())) {
            throw new DataAlreadyRegisteredException("Cpf ou email ja cadastrados.");
        }

        Client client = clientMapper.toClient(clientRequest);
        Client savedClient = clientRepository.save(client);
        return clientMapper.toClientResponse(savedClient);
    }


    public Client findClientByIdEntity(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente com id " + id + " não encontrado."));
    }

    public ClientResponse findClientById(Long id) {
        Client client = findClientByIdEntity(id);
        return clientMapper.toClientResponse(client);
    }


}
