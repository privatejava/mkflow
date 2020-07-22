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

package com.mkflow.model;

import com.mkflow.model.auth.AuthenticationMethod;

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
