package com.mkflow.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BobFlowSpec {
    @JsonProperty("cloud.vendor")
    private CloudVendor type;
}
