package com.mkflow.dto;

import com.mkflow.model.CloudVendor;
import com.mkflow.model.ProvisionType;


public class RunServerDTO {
    private CloudVendor type;

    private ConnectionParamDTO params;

    private String commands;

    private ProvisionType provisionType;

    private CodebaseDTO codebase;

    public CloudVendor getType() {
        return type;
    }

    public void setType(CloudVendor type) {
        this.type = type;
    }

    public ConnectionParamDTO getParams() {
        return params;
    }

    public void setParams(ConnectionParamDTO params) {
        this.params = params;
    }


    public ProvisionType getProvisionType() {
        return provisionType;
    }

    public void setProvisionType(ProvisionType provisionType) {
        this.provisionType = provisionType;
    }

    public String getCommands() {
        return commands;
    }

    public void setCommands(String commands) {
        this.commands = commands;
    }

    public CodebaseDTO getCodebase() {
        return codebase;
    }

    public void setCodebase(CodebaseDTO codebase) {
        this.codebase = codebase;
    }
}
