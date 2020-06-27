package com.mkflow.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public interface IAMPermission {
    public List<String> getActions();

    public List<String> getResources();

    public boolean isAllowed();
}
