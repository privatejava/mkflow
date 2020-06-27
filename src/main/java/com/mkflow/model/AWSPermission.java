package com.mkflow.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class AWSPermission implements IAMPermission {
    private static final Logger log = LogManager.getLogger(AWSPermission.class);

    @JsonProperty("Action")
    private List<String> action;

    @JsonProperty("Resource")
    private List<String> resource;

    @JsonProperty("Effect")
    private String effect;


//
//    public PolicyStatement getStatement(){
//        log.debug("{}, {}, {}", isAllowed()?Effect.ALLOW:Effect.DENY,action ,resource);
//        PolicyStatement statement = new PolicyStatement();
//        statement.setEffect(Effect.ALLOW);
//        statement.addAllResources();
//        statement.addActions("s3:*");
//        return statement;
////        return PolicyStatement.Builder.create().effect(isAllowed()?Effect.ALLOW:Effect.DENY)
////            .actions(Arrays.asList("s3:*")).resources(Arrays.asList("*")).build();
//    }

    @Override
    @JsonIgnore
    public List<String> getActions() {
        return action;
    }

    @Override
    @JsonIgnore
    public List<String> getResources() {
        return resource;
    }


    @Override
    @JsonIgnore
    public boolean isAllowed() {
        return this.effect.equalsIgnoreCase("Allow");
    }

    public String getEffect() {
        return this.effect;
    }

    public void setEffect(String effect) {
        this.effect = effect;
    }

    public List<String> getAction() {
        return action;
    }

    public void setAction(List<String> action) {
        this.action = action;
    }

    public List<String> getResource() {
        return resource;
    }

    public void setResource(List<String> resource) {
        this.resource = resource;
    }

    @Override
    public String toString() {
        return "AWSPermission{" +
            "action=" + action +
            ", resource=" + resource +
            ", effect='" + effect + '\'' +
            '}';
    }
}
