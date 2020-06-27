package com.mkflow.controller;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/log")
public class WebController {

    @Inject
    Template log;


    @GET
    @Produces({MediaType.TEXT_HTML})
    public TemplateInstance getLog() {
        return log.data("jobId", "");
    }
}
