package com.mkflow.model;

import com.mkflow.model.auth.Authentication;

public class Cloud {
    private CloudVendor vendor;

    private Authentication auth;

    private Provision provision;

    public CloudVendor getVendor() {
        return vendor;
    }

    public void setVendor(CloudVendor vendor) {
        this.vendor = vendor;
    }

    public Provision getProvision() {
        return provision;
    }

    public void setProvision(Provision provision) {
        this.provision = provision;
    }

    public Authentication getAuth() {
        return auth;
    }

    public void setAuth(Authentication auth) {
        this.auth = auth;
    }
}
