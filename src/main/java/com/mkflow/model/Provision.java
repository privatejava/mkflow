package com.mkflow.model;

import java.util.List;

public class Provision {
    private ProvisionType type;

    private String instanceType;

    private List<IAMPermission> permission;

    public ProvisionType getType() {
        return type;
    }

    public void setType(ProvisionType type) {
        this.type = type;
    }

    public String getInstanceType() {
        return instanceType;
    }

    public void setInstanceType(String instanceType) {
        this.instanceType = instanceType;
    }

    public List<IAMPermission> getPermission() {
        return permission;
    }

    public void setPermission(List<IAMPermission> permission) {
        this.permission = permission;
    }
}
