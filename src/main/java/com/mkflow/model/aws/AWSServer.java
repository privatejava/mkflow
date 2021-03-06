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

package com.mkflow.model.aws;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.mkflow.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.ec2.model.SpotInstanceRequest;

import java.util.concurrent.CompletableFuture;

public class AWSServer extends Server<SpotInstanceRequest> {
    private static final Logger log = LoggerFactory.getLogger(AWSServer.class);

    @JsonIgnore
    private Session session;

    private AWSProvisioner provisioner;

    public AWSServer() {
        super();
        Cloud cloud = new Cloud();
        cloud.setVendor(CloudVendor.AMAZON);
        setCloud(cloud);
        provisioner = new AWSProvisioner();
        addBuildspecParser(new AWSBuildspecParser());
    }

    @Override
    public Session getSession() throws JSchException {
        if (session == null || !session.isConnected()) {
            connect();
        }
        return session;
    }

    @Override
    public Session getCurrentSession() {
        return session;
    }


    @Override
    public void connect() throws JSchException {

        Container container = getContainer();
        switch (container.getAuth().getType()) {
            case KEY:
                connectByKey(container.getAuth());
                break;
            case HTTP:
            case USER_PASS:
                throw new UnsupportedOperationException();
        }
    }

    @Override
    public CompletableFuture<SpotInstanceRequest> marketProvision(Server server) {
        return provisioner.marketProvision(server);
    }

    @Override
    public CompletableFuture<SpotInstanceRequest> onPremiseProvision(Server server) {
        return provisioner.onPremiseProvision(server);
    }

    @Override
    public CompletableFuture<SpotInstanceRequest> onDemandProvision(Server server) {
        return provisioner.onDemandProvision(server);
    }

    @Override
    public void cancelProvision() {
        provisioner.cancelProvision();
    }

    @Override
    public AWSProvisioner getProvisioner() {
        return provisioner;
    }


    protected void connectByKey(AuthDetail detail) throws JSchException {
        JSch jsch = new JSch();
        log.debug("Using key from {}", getWorkDir().resolve("id_rsa"));
        jsch.addIdentity(getWorkDir().resolve("id_rsa").toString());
        log.debug("Connecting to {} ... using user {}", provisioner.getInstance().publicIpAddress(), detail.getUsername());

        session = jsch.getSession(detail.getUsername(), provisioner.getInstance().publicIpAddress());
        session.setConfig("StrictHostKeyChecking", "no");
        int MAX_TRY = 5;
        for (int i = 0; i < MAX_TRY; i++) {
            try {
                session.connect(60000);
                break;
            } catch (JSchException ex) {
                try {
                    Thread.sleep(5000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (i == MAX_TRY - 1)
                    throw ex;
                continue;
            }
        }

        if (session.isConnected()) {
            log.debug("Successfully connected to server");
        }
    }

}
