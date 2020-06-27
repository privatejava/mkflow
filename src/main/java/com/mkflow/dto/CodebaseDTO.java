package com.mkflow.dto;

import java.net.URI;

public class CodebaseDTO {
    private ConnectionParamDTO param;

    private URI uri;

    public ConnectionParamDTO getParam() {
        return param;
    }

    public void setParam(ConnectionParamDTO param) {
        this.param = param;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }
}
