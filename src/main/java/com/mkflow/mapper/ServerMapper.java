package com.mkflow.mapper;

import com.mkflow.dto.ServerDTO;
import com.mkflow.model.Server;
import org.mapstruct.Mapper;
import org.mapstruct.Mappings;

@Mapper(componentModel = "cdi")
public interface ServerMapper {
    @Mappings({})
    ServerDTO toDTO(Server server);
}
