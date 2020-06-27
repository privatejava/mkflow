package com.mkflow.model;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum BuildCacheType {
    S3("s3");

    private String shortName;

    BuildCacheType(String shortName) {
        this.shortName = shortName;
    }

    @JsonCreator
    public static BuildCacheType parse(String shortName) {
        for (BuildCacheType t : values()) {
            if (t.shortName.equalsIgnoreCase(shortName)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Invalid provision type provided.");
    }

    @JsonValue
    public String getShortName() {
        return shortName;
    }
}
