package com.mkflow.model;


public class ConnectionParam {
    private AuthenticationMethod authMethod;

    private AuthDetail detail;

    public AuthenticationMethod getAuthMethod() {
        return authMethod;
    }

    public void setAuthMethod(AuthenticationMethod authMethod) {
        this.authMethod = authMethod;
    }

    public AuthDetail getDetail() {
        return detail;
    }

    public void setDetail(AuthDetail detail) {
        this.detail = detail;
    }
}
