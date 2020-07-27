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

package com.mkflow.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RegisterForReflection
public class AWSPermission implements IAMPermission {
    private static final Logger log = LoggerFactory.getLogger(AWSPermission.class);

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
