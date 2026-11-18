package com.thiago.sistema_bancario.controller;

import com.thiago.sistema_bancario.dto.ClientRequest;
import com.thiago.sistema_bancario.dto.ClientResponse;
import com.thiago.sistema_bancario.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {
    private final ClientService clientService;

    @PostMapping
    public ResponseEntity<ClientResponse> registerClient(@RequestBody @Valid ClientRequest clientRequest) {
        return new ResponseEntity<>(clientService.registerClient(clientRequest), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientResponse> findClientById(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.findClientById(id));
    }
}
