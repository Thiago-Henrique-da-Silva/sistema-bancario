package com.thiago.sistema_bancario.Mapper;

import com.thiago.sistema_bancario.domain.Extract;
import com.thiago.sistema_bancario.dto.ExtractResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ExtractMapper {

    @Mapping(target = "numberAccount", source = "account.number")
    ExtractResponse toExtractResponse(Extract extract);
}
