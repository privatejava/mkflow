package com.mkflow.model;

public class Cloud {
    private CloudVendor vendor;

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
}
