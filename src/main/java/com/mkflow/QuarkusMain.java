package com.mkflow;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;

@io.quarkus.runtime.annotations.QuarkusMain
public class QuarkusMain implements QuarkusApplication {
    @Override
    public int run(String... args) {
        Quarkus.waitForExit();
        return 0;
    }
}
