/*
 * Copyright 2020 Mkflow
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
