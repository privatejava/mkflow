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

package com.mkflow.dto;

import com.mkflow.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class ServerDTO {
    private static final Logger log = LoggerFactory.getLogger(Server.class);

    protected List<BuildSpecParser> buildspecParsers;

    private CloudVendor type;

    private ProvisionType provisionType;

    private ServerSpec spec;

    private ConnectionParam params;

    private Buildspec buildspec;

    private CodebaseDTO codebase;

    public ServerSpec getSpec() {
        return spec;
    }

    public void setSpec(ServerSpec spec) {
        this.spec = spec;
    }

    public CloudVendor getType() {
        return type;
    }

    public void setType(CloudVendor type) {
        this.type = type;
    }

    public ProvisionType getProvisionType() {
        return provisionType;
    }

    public void setProvisionType(ProvisionType provisionType) {
        this.provisionType = provisionType;
    }

    public ConnectionParam getParams() {
        return params;
    }

    public void setParams(ConnectionParam params) {
        this.params = params;
    }

    public Buildspec getBuildspec() {
        return buildspec;
    }

    public void setBuildspec(Buildspec buildspec) {
        this.buildspec = buildspec;
    }

    public List<BuildSpecParser> getBuildspecParsers() {
        return buildspecParsers;
    }

    public void setBuildspecParsers(List<BuildSpecParser> buildspecParsers) {
        this.buildspecParsers = buildspecParsers;
    }
}
