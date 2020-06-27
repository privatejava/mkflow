package com.mkflow.model;

import java.net.URI;

public class Codebase {
    private ConnectionParam param;

    private URI uri;

    public ConnectionParam getParam() {
        return param;
    }

    public void setParam(ConnectionParam param) {
        this.param = param;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }
}
