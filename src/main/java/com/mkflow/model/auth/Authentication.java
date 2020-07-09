package com.mkflow.model.auth;

public abstract class Authentication<T> {
    private final AuthenticationMethod type;

    protected Authentication(AuthenticationMethod type) {
        this.type = type;
    }

    public abstract T getParams();

    public AuthenticationMethod getType() {
        return type;
    }


}
