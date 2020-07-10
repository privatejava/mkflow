package com.mkflow.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.jackson.ObjectMapperCustomizer;
import javax.inject.Singleton;

@Singleton
public class AutoJacksonModule implements ObjectMapperCustomizer {
    public void customize(ObjectMapper mapper) {
        mapper.findAndRegisterModules();
    }
}
