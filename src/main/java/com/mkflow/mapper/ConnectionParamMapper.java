package com.mkflow.mapper;

import com.mkflow.dto.ConnectionParamDTO;
import com.mkflow.model.ConnectionParam;
import com.mkflow.model.KeyAuthDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mappings;

@Mapper(componentModel = "cdi")
public interface ConnectionParamMapper {

    @Mappings({})
    default ConnectionParam fromDTO(ConnectionParamDTO detail) {
        KeyAuthDetail authDetail = new KeyAuthDetail(detail.getDetail().getPrivateKey(), detail.getDetail().getPublicKey(),
            detail.getDetail().getUsername(), detail.getDetail().getPassword());
        ConnectionParam obj = new ConnectionParam();
        obj.setAuthMethod(detail.getAuthMethod());
        obj.setDetail(authDetail);
        return obj;
    }
}
