package com.mkflow.dto;

import com.mkflow.model.AuthenticationMethod;

public class ConnectionParamDTO {
    private AuthenticationMethod authMethod;

    private AuthDetailDTO detail;

    public AuthenticationMethod getAuthMethod() {
        return authMethod;
    }

    public void setAuthMethod(AuthenticationMethod authMethod) {
        this.authMethod = authMethod;
    }

    public AuthDetailDTO getDetail() {
        return detail;
    }

    public void setDetail(AuthDetailDTO detail) {
        this.detail = detail;
    }
}
