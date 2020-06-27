package com.mkflow.model;


import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;

public class S3CacheProvider implements CacheProvider {
    private static final Logger log = LogManager.getLogger(S3CacheProvider.class);

    private Session session;

    public S3CacheProvider(Session session) {
        this.session = session;
    }

    public void waitUntilChannelTimeout(Channel channel) throws InterruptedException {
        long timeout = 300000L;
        while (timeout > 0L) {
            if (channel.isClosed()) {
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
            exec.disconnect();
        }
    }
}
