package com.mkflow.dto;

public class AuthDetailDTO {
    private String privateKey;

    private String publicKey;

    private String username;

    private String password;

    private String host;

    private int port;

    public AuthDetailDTO() {

    }

    public AuthDetailDTO(String privateKey, String publicKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    public AuthDetailDTO(String privateKey, String publicKey, String username) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.username = username;
    }

    public AuthDetailDTO(String privateKey, String publicKey, String username, String password) {
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
}
