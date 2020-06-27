package com.mkflow.model;


import java.util.concurrent.CompletableFuture;

public interface ProvisionerFactory<T> {
    default CompletableFuture<T> marketProvision(Server server) {
        throw new UnsupportedOperationException();
    }

    default CompletableFuture<T> onPremiseProvision(Server server) {
        throw new UnsupportedOperationException();
    }

    default CompletableFuture<T> onDemandProvision(Server server) {
        throw new UnsupportedOperationException();
    }

    void cancelProvision();
}
