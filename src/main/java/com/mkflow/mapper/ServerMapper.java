package com.mkflow.mapper;

import com.mkflow.dto.ServerDTO;
import com.mkflow.model.Server;

//@Mapper(componentModel = "cdi")
public interface ServerMapper {
//    @Mappings({})
    ServerDTO toDTO(Server server);
}
