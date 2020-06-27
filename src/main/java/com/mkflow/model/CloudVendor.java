package com.mkflow.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonFormat
public enum CloudVendor {
    AMAZON("aws"), AZURE("azure"), GOOGLE("google");

    private String shortName;

    CloudVendor(String shortName) {
        this.shortName = shortName;
    }

    @JsonCreator
    public static CloudVendor parse(String shortName) {
        for (CloudVendor t : values()) {
            if (t.shortName.equalsIgnoreCase(shortName)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Invalid cloud type provided");
    }

    @JsonValue
    public String getShortName() {
        return shortName;
    }
}

