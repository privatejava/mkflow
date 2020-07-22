/*
 * Copyright 2020 Mkflow
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
