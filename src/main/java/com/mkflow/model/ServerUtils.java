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
import com.mkflow.model.aws.AWSServer;

public class ServerUtils {
    public static Server create(CloudVendor type, String commandPath, AuthenticationMethod method,
                                String username, String password, ProvisionType provisionType, Codebase codebase) throws Exception {
        Server server = null;
        switch (type) {
            case AMAZON:
                server = new AWSServer();
                Container container = new Container();
                if (method == AuthenticationMethod.KEY) {
                    KeyAuthDetail detail = new KeyAuthDetail("", "", username);
                    container.setAuth(detail);
                    detail.setType(AuthenticationMethod.KEY);
                }
                Provision provision = new Provision();
                provision.setType(ProvisionType.MARKET);
                server.getCloud().setProvision(provision);
                server.setCodebase(codebase);
                server.setContainer(container);
//                if(commandPath != null){
                server.loadBuildspec(commandPath);
//                }
                break;

        }
        return server;
    }

    public static Server create(CloudVendor type, String commandPath, AuthenticationMethod method,
                                String username, String password, ProvisionType provisionType) throws Exception {
        return create(type, commandPath, method, username, password, provisionType, null);
    }
}
