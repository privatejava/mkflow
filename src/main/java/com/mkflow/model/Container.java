package com.mkflow.model;

public class Container {
    private String image;

    private KeyAuthDetail auth;


    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public KeyAuthDetail getAuth() {
        return auth;
    }

    public void setAuth(KeyAuthDetail auth) {
        this.auth = auth;
    }
}
