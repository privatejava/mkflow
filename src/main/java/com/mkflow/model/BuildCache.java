package com.mkflow.model;

public class BuildCache {
    private BuildCacheType type;

    private String location;

    public BuildCacheType getType() {
        return type;
    }

    public void setType(BuildCacheType type) {
        this.type = type;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
