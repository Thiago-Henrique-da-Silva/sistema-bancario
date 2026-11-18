package com.thiago.sistema_bancario.Mapper;

import com.thiago.sistema_bancario.domain.Client;
import com.thiago.sistema_bancario.dto.ClientRequest;
import com.thiago.sistema_bancario.dto.ClientResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface ClientMapper {

    @Mapping(target = "id", ignore = true)
    Client toClient(ClientRequest request);

    ClientResponse toClientResponse(Client client);
}
