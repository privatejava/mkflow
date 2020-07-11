package com.mkflow;

import com.mkflow.model.CloudVendor;
import com.mkflow.model.ProvisionType;
import com.mkflow.model.Server;
import com.mkflow.model.ServerUtils;
import com.mkflow.model.auth.AuthenticationMethod;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;


public class Runner {
    private static final Logger log = LoggerFactory.getLogger(Runner.class);

    public static void main(String[] args) {
        CommandLineParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption(null, "region", true, "Running in for specific region");
        options.addOption("c", "cloud", true, "For AWS cloud only");
        options.addOption(null, "aws-profile", true, "For AWS profile name ");
        options.addOption("i", "input", true, "Commands yaml files");
        options.addOption("a", "auth", true, "Authentication Method KEY, USER_PASS, HTTP");
        options.addOption("u", "user", true, "User name to login to machine");
        options.addOption("p", "pass", true, "Password to authenticate user");
        options.addOption("t", "type", true, "Provision Type:  [market / demand / premise] ");
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("mkflow [OPTION]... [FILE]\n" +
            "Run the commands in cloud machine.\n\n", options, true);
        Server server = null;
        try {
            // parse the command line arguments
            CommandLine line = parser.parse(options, args);

            // validate that block-size has been set
            if (!line.hasOption("cloud")) {
                // print the value of block-size
                throw new CliValidationException("Please select the cloud eg. --cloud aws or -c azure ");
            }
            if (!line.hasOption("input")) {
                // print the value of block-size
                throw new CliValidationException("Please select the input command file eg. --input commands.yml");
            }
            if (!line.hasOption("user")) {
                // print the value of block-size
                throw new CliValidationException("Please provide username to login the machine");
            }
            if (!line.hasOption("auth")) {
                // print the value of block-size
                throw new CliValidationException("Authentication method not provided.");
            }
            log.debug("Checking 1");
            String cloudTypeStr = line.getOptionValue("cloud");
            String username = line.getOptionValue("user");
            String password = line.getOptionValue("pass");
            String input = line.getOptionValue("input");
            String region = line.getOptionValue("region");
            File file = new File(System.getProperty("user.dir")).toPath().resolve(input).toFile();
            if (!file.exists()) {
                throw new CliValidationException("Cannot find `" + file + "` file ");
            }
//            if (line.getOptionValue("aws-profile") != null) {
//                Utils.setEnv("AWS_PROFILE", line.getOptionValue("aws-profile"));
//            }
//            if (line.getOptionValue("region") != null) {
//                Utils.setEnv("AWS_REGION", region);
//            }

            log.debug("Checking 1");
            CloudVendor cloudVendor = CloudVendor.parse(cloudTypeStr);
            ProvisionType provisionType = ProvisionType.parse(line.getOptionValue("type"));
            AuthenticationMethod authenticationMethod = AuthenticationMethod.parse(line.getOptionValue("auth"));

            server = ServerUtils.create(cloudVendor, file.getAbsolutePath(), authenticationMethod, username, password, provisionType);
            final Server s = server;
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    if (s != null) {
                        s.cancelProvision();
                    }
                }
            });
            server.provision().get();
            Thread.sleep(60000);
            server.connect();
            log.debug("Executing the commands ");
            server.execute(true);
            log.debug("Value {}", cloudVendor);
        } catch (Exception exp) {
            exp.printStackTrace();
            System.out.println("");
            System.out.println("Error: " + exp.getMessage());
            log.debug("{}", exp.getMessage(), exp);
        } finally {
            final Server s = server;
            if (s != null) {
                s.cancelProvision();
            }
        }
    }
}
