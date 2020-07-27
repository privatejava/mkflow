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

package com.mkflow.model.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;

@RegisterForReflection
public class AWSBasicAuthentication extends Authentication<AwsCredentials> {

    private AwsCredentials credentials;

    public AWSBasicAuthentication(@JsonProperty("AWS_ACCESS_KEY_ID") String id, @JsonProperty("AWS_SECRET_ACCESS_KEY") String secret) {
        super(AuthenticationMethod.PARAMS);
        credentials = AwsBasicCredentials.create(id, secret);
    }

    @Override
    public AwsCredentials getParams() {
        return credentials;
    }


}
