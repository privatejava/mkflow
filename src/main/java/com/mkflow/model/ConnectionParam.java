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


import com.mkflow.model.auth.AuthenticationMethod;

public class ConnectionParam {
    private AuthenticationMethod authMethod;

    private AuthDetail detail;

    public AuthenticationMethod getAuthMethod() {
        return authMethod;
    }

    public void setAuthMethod(AuthenticationMethod authMethod) {
        this.authMethod = authMethod;
    }

    public AuthDetail getDetail() {
        return detail;
    }

    public void setDetail(AuthDetail detail) {
        this.detail = detail;
    }
}
