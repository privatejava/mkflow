package com.mkflow;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.mkflow.utils.JschFileTransfer;
import com.mkflow.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TesterClass {
    private static final Logger log = LoggerFactory.getLogger(TesterClass.class);

    public static void main(String[] args) throws JSchException, IOException {
        String ip = "18.182.19.145";
        String user = "ec2-user";
        File dir = Path.of("/tmp/5cf80693-ab7b-48b5-b2f3-5ec0500f806e18441066668234501747").toFile();
        System.out.println("hello");
        JSch jsch = new JSch();
        log.info("Using key from {}", dir.toPath().resolve("id_rsa"));
        log.debug("{}", new String(Files.readAllBytes(dir.toPath().resolve("id_rsa"))));
        jsch.addIdentity(dir.toPath().resolve("id_rsa").toString());
        log.info("Connecting to {} ... using user {}", ip, user);
        Session session = jsch.getSession(user, ip);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect(40000);
        if (session.isConnected()) {
            log.info("Successfully connected to server");
        }
        File zipFile = dir.toPath().resolve("codebase.zip").toFile();
        log.debug("{} -> {}", dir.toPath().resolve("src").toString(), zipFile.getAbsolutePath());
        Utils.zip(dir.toPath().resolve("src").toString() + "/", zipFile.getAbsolutePath());
        ChannelExec channelSftp = (ChannelExec) session.openChannel("exec");


        try {
//            SSHExec sshExec = new SSHExec();
//            sshExec.
//            String destdir=channelSftp.setCommand("pwd");
            log.debug("Copying files");
            JschFileTransfer.uploadFile(session, zipFile, ".");
            log.debug("Copying complete");
            channelSftp.setCommand("unzip -qo codebase.zip && rm  -rf codebase.zip");
            channelSftp.connect();
        } catch (Exception e) {
            throw new JSchException(e.toString());
        }
        session.disconnect();
    }
}
