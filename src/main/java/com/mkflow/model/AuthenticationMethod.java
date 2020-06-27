package com.mkflow.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonFormat
public enum AuthenticationMethod {
    KEY("key"), USER_PASS("user_pass"), HTTP("http"), TOKEN("token");

    private String shortName;

    AuthenticationMethod(String shortName) {
        this.shortName = shortName;
    }

    @JsonCreator
    public static AuthenticationMethod parse(String auth) {
        for (AuthenticationMethod a : values()) {
            if (a.shortName.equalsIgnoreCase(auth)) {
                return a;
            }
        }
        throw new IllegalArgumentException("Invalid Authentication Method provided");
    }

    @JsonValue
    public String getShortName() {
        return shortName;
    }
}
