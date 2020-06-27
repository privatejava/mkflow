package com.mkflow.model;

public class KeyAuthDetail implements AuthDetail {
    private String privateKey;

    private String publicKey;

    private String username;

    private String password;

    private String host;

    private int port;

    private AuthenticationMethod type;

    public KeyAuthDetail() {
    }

    public KeyAuthDetail(String privateKey, String publicKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    public KeyAuthDetail(String privateKey, String publicKey, String username) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.username = username;
    }

    public KeyAuthDetail(String privateKey, String publicKey, String username, String password) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.username = username;
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public AuthenticationMethod getType() {
        return type;
    }

    public void setType(AuthenticationMethod type) {
        this.type = type;
    }
}
