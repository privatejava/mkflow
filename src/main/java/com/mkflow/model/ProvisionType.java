package com.mkflow.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonFormat
public enum ProvisionType {
    MARKET("market"), DEMAND("demand"), ON_PREMISE("premise");

    private String shortName;

    ProvisionType(String shortName) {
        this.shortName = shortName;
    }

    @JsonCreator
    public static ProvisionType parse(String shortName) {
        for (ProvisionType t : values()) {
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
