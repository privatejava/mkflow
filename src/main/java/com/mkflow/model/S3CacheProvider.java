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


import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;

public class S3CacheProvider implements CacheProvider {
    private static final Logger log = LoggerFactory.getLogger(S3CacheProvider.class);

    private Session session;

    public S3CacheProvider(Session session) {
        this.session = session;
    }

    public void waitUntilChannelTimeout(Channel channel) throws InterruptedException {
        long timeout = 300000L;
        while (timeout > 0L) {
            if (channel.isClosed()) {
                log.debug("Exit Status: {}", channel.getExitStatus());
                break;
            }
            Thread.sleep(1000L);
            timeout -= 1000L;
        }
    }

    @Override
    public void download(String source, String target) throws Exception {
        if (session.isConnected()) {
            log.debug("Dowloading {} to {}", source, target);
            String hash = DatatypeConverter.printHexBinary(MessageDigest.getInstance("MD5").digest(target.getBytes()));
            log.debug("Digest: {}", hash);
            String command = String.format("sudo bash -c 'mkdir -p %s && cd %s && aws s3 cp '%s/%s.tar.gz' - | zcat | tar -C %s -xv'",
                target, target, source, hash, target);
            log.debug("Command {}", command);

            ChannelExec exec = (ChannelExec) session.openChannel("exec");
            exec.setCommand(command);
            exec.connect(60000);
            waitUntilChannelTimeout(exec);
            exec.disconnect();
        }
    }

    @Override
    public void upload(String source, String target) throws Exception {
        if (session.isConnected()) {

            log.debug("Uploading {} to {}", source, target);
            String hash = DatatypeConverter.printHexBinary(MessageDigest.getInstance("MD5").digest(source.getBytes()));
            log.debug("Digest: {}", hash);
            String command = String.format("sudo bash -c '[[ -d \"%s\" ]] && cd %s && tar -c . | gzip | aws s3 cp - \"%s/%s.tar.gz\"'",
                source, source, target, hash);
            log.debug("Command {}", command);
            ChannelExec exec = (ChannelExec) session.openChannel("exec");
            exec.setCommand(command);
            exec.connect(60000);
            waitUntilChannelTimeout(exec);
            exec.disconnect();
        }
    }
}
