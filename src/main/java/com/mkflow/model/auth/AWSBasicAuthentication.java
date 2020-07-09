package com.mkflow.model.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;

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
