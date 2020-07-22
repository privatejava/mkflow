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

import com.mkflow.model.auth.Authentication;

public class Cloud {
    private CloudVendor vendor;

    private Authentication auth;

    private Provision provision;

    public CloudVendor getVendor() {
        return vendor;
    }

    public void setVendor(CloudVendor vendor) {
        this.vendor = vendor;
    }

    public Provision getProvision() {
        return provision;
    }

    public void setProvision(Provision provision) {
        this.provision = provision;
    }

    public Authentication getAuth() {
        return auth;
    }

    public void setAuth(Authentication auth) {
        this.auth = auth;
    }
}
