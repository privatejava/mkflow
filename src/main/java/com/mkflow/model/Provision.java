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

import java.util.List;

public class Provision {
    private ProvisionType type;

    private List<String> instanceType;

    private String region;

    private List<IAMPermission> permission;

    public ProvisionType getType() {
        return type;
    }

    public void setType(ProvisionType type) {
        this.type = type;
    }

    public List<String> getInstanceType() {
        return instanceType;
    }

    public void setInstanceType(List<String> instanceType) {
        this.instanceType = instanceType;
    }

    public List<IAMPermission> getPermission() {
        return permission;
    }

    public void setPermission(List<IAMPermission> permission) {
        this.permission = permission;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getRegion() {
        return region;
    }
}
