package com.mkflow.mapper;

import com.mkflow.dto.CodebaseDTO;
import com.mkflow.model.Codebase;
import org.mapstruct.Mapper;
import org.mapstruct.Mappings;

@Mapper(componentModel = "cdi", uses = ConnectionParamMapper.class)
public interface CodebaseMapper {

    @Mappings({

    })
    Codebase fromDTO(CodebaseDTO codebase);
}
