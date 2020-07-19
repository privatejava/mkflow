package com.mkflow.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.jackson.ObjectMapperCustomizer;
import org.jboss.resteasy.plugins.providers.jackson.ResteasyJackson2Provider;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

@Provider
@Consumes({"application/json", "application/*+json", "text/json"})
@Produces({"application/json", "application/*+json", "text/json"})
public class AutoJacksonModule extends ResteasyJackson2Provider {
    private ObjectMapper mapper = new ObjectMapper(); // my own object mapper


    @Override
    public ObjectMapper locateMapper(Class<?> type, MediaType mediaType) {
        mapper.findAndRegisterModules();
        return mapper;
    }
}
