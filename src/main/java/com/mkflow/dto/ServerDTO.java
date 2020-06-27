package com.mkflow.dto;

import com.mkflow.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;


public class ServerDTO {
    private static final Logger log = LogManager.getLogger(Server.class);

    protected List<BuildSpecParser> buildspecParsers;

    private CloudVendor type;

    private ProvisionType provisionType;

    private ServerSpec spec;

    private ConnectionParam params;

    private Buildspec buildspec;

    private CodebaseDTO codebase;

    public ServerSpec getSpec() {
        return spec;
    }

    public void setSpec(ServerSpec spec) {
        this.spec = spec;
    }

    public CloudVendor getType() {
        return type;
    }

    public void setType(CloudVendor type) {
        this.type = type;
    }

    public ProvisionType getProvisionType() {
        return provisionType;
    }

    public void setProvisionType(ProvisionType provisionType) {
        this.provisionType = provisionType;
    }

    public ConnectionParam getParams() {
        return params;
    }

    public void setParams(ConnectionParam params) {
        this.params = params;
    }

    public Buildspec getBuildspec() {
        return buildspec;
    }

    public void setBuildspec(Buildspec buildspec) {
        this.buildspec = buildspec;
    }

    public List<BuildSpecParser> getBuildspecParsers() {
        return buildspecParsers;
    }

    public void setBuildspecParsers(List<BuildSpecParser> buildspecParsers) {
        this.buildspecParsers = buildspecParsers;
    }
}
