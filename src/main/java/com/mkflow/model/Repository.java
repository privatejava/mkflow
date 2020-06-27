package com.mkflow.model;

public class Repository {
    private String uri;

    private KeyAuthDetail auth;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public KeyAuthDetail getAuth() {
        return auth;
    }

    public void setAuth(KeyAuthDetail auth) {
        this.auth = auth;
    }
}
