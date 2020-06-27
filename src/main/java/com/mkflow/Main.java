package com.mkflow;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.mkflow.model.Provision;
import com.mkflow.model.ProvisionType;
import com.mkflow.model.aws.AWSProvisioner;
import com.mkflow.model.aws.AWSServer;
import com.mkflow.utils.Utils;
import io.quarkus.runtime.QuarkusApplication;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.Instance;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class Main implements QuarkusApplication {
    private static final Logger log = LogManager.getLogger(Main.class);

    @Override
    public int run(String... args) throws Exception {
        AWSServer server = new AWSServer();
        try {
            Provision provision = new Provision();
            provision.setType(ProvisionType.MARKET);
            server.getCloud().setProvision(provision);
            AWSProvisioner provisioner = server.getProvisioner();
            // For provision
//            CompletableFuture<SpotInstanceRequest> provision = server.provision();
//            SpotInstanceRequest spotInstanceRequest = provision.get();
//            AWSProvisioner provisioner = server.getProvisioner();
//            log.debug("Sleeping 30 sec " );
//            Thread.sleep(30000L);

            String instanceId = "i-01cb172dd357611a6";// spotInstanceRequest.instanceId();
            Instance instance = Ec2Client.create().describeInstances(c -> c.instanceIds(instanceId)).reservations().get(0).instances().get(0);
            JSch jsch = new JSch();
            jsch.addIdentity(Utils.getSshDir().toPath().resolve("id_rsa").toString());
            log.debug("Connecting to : {}", instance.publicIpAddress());
            Session session = jsch.getSession("ec2-user", instance.publicIpAddress());
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(30000);
            List<String> commands = new ArrayList<>();
            commands.add("ls -al");
            commands.add("pwd");
            commands.add("whoami");
            commands.add("apt install");
            commands.add("ls -al");

            for (String s : commands) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ByteArrayOutputStream err = new ByteArrayOutputStream();
                ByteArrayInputStream in = new ByteArrayInputStream(s.getBytes());
                log.debug("Executing: {}", s);
                ChannelExec channel = (ChannelExec) session.openChannel("exec");
                channel.setCommand(s);
//                InputStream commandOutput = channel.getExtInputStream();
                channel.setErrStream(err);
                channel.setInputStream(in);
                channel.setOutputStream(out);
                channel.connect(3000);
//                while(true){
//                    if (channel.isClosed()) {
//                        if ((out.available() > 0) || (err. > 0)) continue;
//                        System.out.println("exit-status: " + channel.getExitStatus());
//                        break;
//                    }
//                    Thread.sleep(1000L);
//                }
                long timeout = 3000L;
                while (timeout > 0L) {
                    if (channel.isClosed()) {
                        break;
                    }
                    Thread.sleep(1000L);
                    timeout -= 1000L;
                }
                if (err.size() > 0) {
                    log.error("Err: {}", new String(err.toByteArray()));
                    break;
                } else {
                    log.debug("Out: {}", new String(out.toByteArray()));
                }
                channel.disconnect();
            }
//            StringInputStream commands = new StringInputStream("ls -al");
//            session.connect(30000);


//            channel.setCommand("ls -al");
//
//            channel.setInputStream(System.in);
//            channel.setOutputStream(System.out);
//            channel.connect(30000);
//            provisioner.generateSSHKey();
//            Ec2Client client = Ec2Client.builder().build();
//            CreateKeyPairResponse key_pair = client.createKeyPair(e -> e.keyName("KEY_PAIR"));
//            log.debug("{}", key_pair.keyMaterial());
            log.debug("{}", Utils.getSshDir());


//            CompletableFuture<StartSessionResponse> ssh = server.getProvisioner().getSSH(instanceId);
//
//            log.debug("URL: {}",ssh.get().streamUrl());
//            log.debug("Session: {}",ssh.get().sessionId());
//            log.debug("Token: {}", ssh.get().tokenValue());

//            Thread.sleep(30000L);
//            log.debug("Current Spot Instance: {}", spotInstanceRequest.instanceId());
        } finally {
//            log.debug("Cancelling Provision..");
//            server.cancelProvision();
        }
        return 10;
    }
}
