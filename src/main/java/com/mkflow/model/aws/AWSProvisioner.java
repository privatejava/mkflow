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


import com.fasterxml.jackson.core.JsonProcessingException;
import com.mkflow.model.AWSPermission;
import com.mkflow.model.ProvisionerFactory;
import com.mkflow.model.Server;
import com.mkflow.model.auth.AWSBasicAuthentication;
import com.mkflow.utils.Utils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.awscore.client.builder.AwsClientBuilder;
import software.amazon.awssdk.core.client.builder.SdkAsyncClientBuilder;
import software.amazon.awssdk.core.client.builder.SdkSyncClientBuilder;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.Ec2ClientBuilder;
import software.amazon.awssdk.services.ec2.model.*;
import software.amazon.awssdk.services.iam.IamAsyncClient;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.*;
import software.amazon.awssdk.services.ssm.SsmAsyncClient;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.StartSessionRequest;
import software.amazon.awssdk.services.ssm.model.StartSessionResponse;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


public class AWSProvisioner implements ProvisionerFactory<SpotInstanceRequest> {
    private static final Logger log = LoggerFactory.getLogger(AWSProvisioner.class);

    private String keyPairName;

    private Instance instance;

    private String securityGroupId;

    private Vpc vpc;

    private Subnet subnet;

    //    private KeyPai
    private InstanceProfile instanceProfile = null;

    private Role role = null;

    private Server server;

    private ArrayList<String> spotInstanceRequestIds = new ArrayList<String>();

    private <T extends AwsClientBuilder> T clientBuilder(T builder) {
        if (server.getCloud().getAuth() != null) {
            if (server.getCloud().getAuth() instanceof AWSBasicAuthentication) {
                AWSBasicAuthentication auth = (AWSBasicAuthentication) server.getCloud().getAuth();
                builder.credentialsProvider(StaticCredentialsProvider.create(auth.getParams()));
            }
        }
        if (server.getCloud().getProvision().getRegion() != null) {
            builder.region(Region.of(server.getCloud().getProvision().getRegion()));
        }
        if(builder instanceof SdkSyncClientBuilder){
            ((SdkSyncClientBuilder)builder).httpClient(software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient.builder().build());
        }
        return builder;
    }

    protected Ec2Client client() {
        Ec2ClientBuilder builder = Ec2Client.builder();
        clientBuilder(builder);
        Ec2Client client = builder.build();
        return client;
    }

    public void refreshInstance() {
        instance = client().describeInstances(c -> c.instanceIds(instance.instanceId())).reservations().get(0).instances().get(0);
    }

    public Instance getInstance() {
        return instance;
    }

    public void loadInstanceById(String instanceId) {
        Ec2Client client = client();
        instance = client.describeInstances(c -> c.instanceIds(instanceId)).reservations().get(0).instances().get(0);
        subnet = client.describeSubnets(c -> c.subnetIds(instance.subnetId())).subnets().get(0);
        vpc = client.describeVpcs(v -> v.vpcIds(subnet.vpcId())).vpcs().get(0);
        keyPairName = client.describeKeyPairs(c -> c.keyNames(instance.keyName())).keyPairs().get(0).keyName();
        securityGroupId = client.describeSecurityGroups(s -> s.groupNames(instance.securityGroups().stream().map(m -> m.groupName()).toArray(String[]::new)))
            .securityGroups().get(0).groupId();
    }

    public StartSessionResponse getSSH(String instanceId) {
        SsmClient client = clientBuilder(SsmClient.builder()).build();
        StartSessionRequest request = StartSessionRequest.builder().target(instanceId)
            .build();
        StartSessionResponse startSessionResponse = client.startSession(request);
        return startSessionResponse;
    }

    public Image getAmiId(String name) throws Exception {
        DescribeImagesRequest request = DescribeImagesRequest.builder()
            .filters(Filter.builder().name("name").values(name).build(),
                Filter.builder().name("architecture").values("x86_64").build())
            .owners("amazon").build();
        DescribeImagesResponse describeImagesResponse = client().describeImages(request);
        Optional<Image> first = describeImagesResponse.images().stream().findFirst();
        if (first.isPresent()) {
            Image image = first.get();
            log.debug("Image:: Id: {} Name: {}", image.imageId(), image.name());
            return image;
        } else {
            throw new Exception("No valid Image found");
        }

    }

    public Subnet getSubnet() {
        if (subnet != null) {
            return subnet;
        }
        Ec2Client client = client();
        String vpcId = getDefaultVPC().vpcId();
        log.debug("Searching subnets in " + vpcId);
        DescribeSubnetsResponse describeSubnetsResponse = client.describeSubnets(c -> c.filters(f -> f.name("vpc-id").values(vpcId)));
        subnet = describeSubnetsResponse.subnets().isEmpty() ? null : describeSubnetsResponse.subnets().get(0);
        log.debug("Using Subnet: {}", subnet);
        return subnet;
    }

    public Vpc getDefaultVPC() {
        if (vpc == null) {
            Ec2Client client = client();
            DescribeVpcsResponse describeVpcsResponse = client.describeVpcs();
            List<Vpc> vpcs = describeVpcsResponse.vpcs();
            Optional<Vpc> aws_default = vpcs.stream()
                .filter(f -> {
                    log.debug("VPC: {} Default: {} CIDR Block: {}", f.vpcId(), f.isDefault(), f.cidrBlock());
                    return f.cidrBlock().startsWith("172") || f.tags().stream()
                        .filter(t -> t.value().toLowerCase().contains("aws default")).findFirst().isPresent();
                })
                .findAny();
            if (aws_default.isPresent()) {
                vpc = aws_default.get();
            } else {
                vpc = vpcs.stream().findFirst().get();
            }
            log.debug("Using VPC: {}", vpc);
        }
        return vpc;
    }

    public String getSecurityGroupId() {
        if (this.securityGroupId == null) {
            Ec2Client client = client();
            Vpc defaultVPC = getDefaultVPC();
            CreateSecurityGroupResponse securityGroup = client.createSecurityGroup(c ->
                c.vpcId(defaultVPC.vpcId())
                    .groupName("mkflow-sg-" + System.currentTimeMillis())
                    .description("MK Flow Security Group - " + new Date()));
            String groupId = securityGroup.groupId();
            client.authorizeSecurityGroupIngress(e -> e.groupId(groupId).ipPermissions(i -> i.ipProtocol("-1").toPort(-1).ipRanges(ip -> ip.cidrIp("0.0.0.0/0"))));
//            client.authorizeSecurityGroupEgress(e -> e.groupId(groupId).ipPermissions(i -> i.ipProtocol("-1").toPort(-1).ipRanges(ip->ip.cidrIp("0.0.0.0/0"))));
            this.securityGroupId = groupId;
            log.debug("Using Security Group: {}", groupId);
        }
        return this.securityGroupId;
    }

    public String getKeyPair() throws IOException {
        final String[] name = new String[]{"KEY_PAIR"};
        Ec2Client client = client();
        Path target = new File(server.getWorkDir() + "/id_rsa").toPath();
        try {
            List<KeyPairInfo> keyPairInfos = client.describeKeyPairs(e -> e.keyNames(name[0])).keyPairs();
            if (!target.toFile().exists()) {
                name[0] = name[0] + System.currentTimeMillis();
                throw new Exception("Not found");
            }
        } catch (Exception exception) {
            CreateKeyPairResponse key_pair = client.createKeyPair(e -> e.keyName(name[0]));
            log.debug("Writing to file: {} > {}", target, key_pair.keyMaterial());
            target.toFile().createNewFile();
            log.debug("File exist:", target.toFile().exists());
            Files.write(target,
                key_pair.keyMaterial().getBytes(), StandardOpenOption.CREATE);


        }
        keyPairName = name[0];
        return keyPairName;
    }


    public void fixIamPermission() throws ExecutionException, InterruptedException {
        IamClient iamClient = clientBuilder(IamClient.builder()).region(Region.AWS_GLOBAL).build();
        ListInstanceProfilesResponse profiles = iamClient.listInstanceProfiles();

        if (!profiles.hasInstanceProfiles()) {
            CreateInstanceProfileResponse ec2_instance_profile = iamClient
                .createInstanceProfile(i -> i.instanceProfileName("EC2_INSTANCE_PROFILE"));
            instanceProfile = ec2_instance_profile.instanceProfile();
        } else {
            Optional<InstanceProfile> hasProfile = profiles.instanceProfiles().stream()
                .filter(f -> f.instanceProfileName().equalsIgnoreCase("EC2_INSTANCE_PROFILE")).findFirst();
            if (!hasProfile.isPresent()) {
                instanceProfile = iamClient.createInstanceProfile(i -> i.instanceProfileName("EC2_INSTANCE_PROFILE"))
                    .instanceProfile();
            } else {
                instanceProfile = hasProfile.get();
            }
        }

        Optional<Role> findRole = iamClient.listRoles().roles().stream().filter(f -> f.roleName()
            .equalsIgnoreCase("EC2_INSTANCE_PROFILE_ROLE")).findFirst();

        if (findRole.isPresent()) {
            role = findRole.get();
        } else {
            role = iamClient.createRole(i -> i.roleName("EC2_INSTANCE_PROFILE_ROLE").assumeRolePolicyDocument("{\n" +
                "  \"Version\": \"2012-10-17\",\n" +
                "  \"Statement\": [\n" +
                "    {\n" +
                "      \"Effect\": \"Allow\",\n" +
                "      \"Principal\": {\n" +
                "        \"Service\": \"ec2.amazonaws.com\"\n" +
                "      },\n" +
                "      \"Action\": \"sts:AssumeRole\"\n" +
                "    }\n" +
                "  ]\n" +
                "}")).role();
        }
        ListInstanceProfilesForRoleResponse attached = iamClient.listInstanceProfilesForRole(r -> r.roleName(role.roleName()));
        boolean foundRelation = false;
        if (attached.hasInstanceProfiles()) {
            Optional<InstanceProfile> foundRel = attached.instanceProfiles().stream().filter(f -> f.instanceProfileName()
                .equalsIgnoreCase("EC2_INSTANCE_PROFILE")).findFirst();
            if (foundRel.isPresent()) {
                foundRelation = true;
            }
        }
        if (!foundRelation) {
            iamClient.addRoleToInstanceProfile(c -> c.instanceProfileName(instanceProfile.instanceProfileName()).roleName(role.roleName()));
        }


        ListAttachedRolePoliciesResponse attachedPolicies = iamClient
            .listAttachedRolePolicies(p -> p.roleName(role.roleName()));

        if (server.getCloud().getProvision().getPermission() != null) {
            Map<String, Object> policyDocument = new HashMap<>();
            policyDocument.put("Version", "2012-10-17");
            policyDocument.put("Statement", (server.getCloud().getProvision().getPermission()
                .stream().map(m -> ((AWSPermission) m)).collect(Collectors.toList())));
            String doc = null;
            try {
                doc = Utils.mapper().writeValueAsString(policyDocument).toString();
                log.debug("Policy Doc: {}", doc);
                GetRolePolicyResponse customPolicy = iamClient.getRolePolicy(c -> c.roleName(role.roleName()).policyName("customPolicy"));
                iamClient.deleteRolePolicy(c -> c.roleName(role.roleName()).policyName("customPolicy"));
                final String policyDoc = doc;
                iamClient.putRolePolicy(c -> c.roleName("EC2_INSTANCE_PROFILE_ROLE").policyName("customPolicy").policyDocument(policyDoc));
                log.debug("Completed creating role");
            } catch ( Exception ex) {
//                ex.printStackTrace();
                log.debug("Exception occured: {}", doc != null);
                if (doc != null) {
                    final String policyDoc = doc;
                    iamClient.putRolePolicy(c -> c.roleName("EC2_INSTANCE_PROFILE_ROLE").policyName("customPolicy").policyDocument(policyDoc));
                    log.debug("Completed update role");
                }

            }
        }

        if (attachedPolicies.hasAttachedPolicies()) {
            Optional<AttachedPolicy> policy = attachedPolicies.attachedPolicies().stream().filter(f -> f.policyName()
                .equalsIgnoreCase("AmazonSSMManagedInstanceCore")).findAny();
            if (!policy.isPresent()) {
                ListPoliciesResponse policies = iamClient.listPolicies(l -> l.maxItems(1000));
                Optional<Policy> first = policies.policies().stream().filter(f -> f.policyName().equalsIgnoreCase("AmazonSSMManagedInstanceCore")).findFirst();
                if (first.isPresent()) {
                    iamClient.attachRolePolicy(p -> p.roleName(role.roleName()).policyArn(first.get().arn()));
                } else {
                    throw new InterruptedException("Cannot find policy: AmazonSSMManagedInstanceCore");
                }
            }
        }

    }

    @Override
    public CompletableFuture<SpotInstanceRequest> marketProvision(Server server) {
        this.server = server;
        log.debug("Provisioning from Market ..");
        List<InstanceType> instanceTypes = server.getCloud().getProvision().getInstanceType() != null &&
            !server.getCloud().getProvision().getInstanceType().isEmpty() ?
            server.getCloud().getProvision().getInstanceType().stream()
                .map(s -> InstanceType.fromValue(s)).collect(Collectors.toList()) :
            Arrays.asList(InstanceType.T2_MICRO);
        log.debug("Instance Type: {}", instanceTypes);
        CompletableFuture<SpotInstanceRequest> completableFuture = new CompletableFuture<>();
        try {
            int[] maxSecond = new int[]{5 * 60}; //seconds
            Ec2Client client = client();
            log.debug("Getting price ..");
            Map<InstanceType, Double> prices = instanceTypes.stream().collect(Collectors.toMap(i -> i, i -> {
                List<SpotPrice> spotPrices = client.describeSpotPriceHistory((c) -> {
                    c.instanceTypes(i).endTime(Instant.now().minus(24, ChronoUnit.HOURS));
                }).spotPriceHistory();
                double avgRate = spotPrices.stream().mapToDouble(m -> Double.parseDouble(m.spotPrice())).average().orElse(Double.NaN);

                return avgRate;
            }));
            Map.Entry<InstanceType, Double> spotPrice = null;
            for (Map.Entry<InstanceType, Double> entry : prices.entrySet()) {
                if (spotPrice == null || entry.getValue().compareTo(spotPrice.getValue()) > 0) {
                    spotPrice = entry;
                }
            }
            final Map.Entry<InstanceType, Double> selectedInstance = spotPrice;

            log.debug("Available : {}", prices);
            log.debug("Selected : {}", selectedInstance);


            Calendar from = Calendar.getInstance();
            from.add(Calendar.SECOND, 2);
            Calendar maxAlive = Calendar.getInstance();
            maxAlive.add(Calendar.SECOND, maxSecond[0]);

            String keyName = getKeyPair();
            fixIamPermission();
            Image image = getAmiId("amzn2-ami-hvm-2.0*");
            //ami-0f310fced6141e627
            RequestSpotInstancesRequest requestSpot = RequestSpotInstancesRequest.builder()
                .spotPrice("" + selectedInstance.getValue()).instanceCount(Integer.valueOf(1))
                .type(SpotInstanceType.ONE_TIME)
                .validUntil(maxAlive.toInstant())
                .blockDurationMinutes(60)
                .launchSpecification(c -> {
                    c.imageId(image.imageId())
                        .iamInstanceProfile(i -> i.arn(instanceProfile.arn()))
                        .subnetId(getSubnet().subnetId())
                        .keyName(keyName)
                        .instanceType(selectedInstance.getKey())
                        .securityGroupIds(getSecurityGroupId());

                }).build();
            log.debug("Launch group {}", requestSpot.launchSpecification());
            RequestSpotInstancesResponse response = client.requestSpotInstances(requestSpot);
            List<SpotInstanceRequest> requests = response.spotInstanceRequests();

            DescribeInstancesResponse instances = client.describeInstances();

            for (SpotInstanceRequest requestResponse : requests) {
                log.debug("Created Spot Request: " + requestResponse.spotInstanceRequestId());
                spotInstanceRequestIds.add(requestResponse.spotInstanceRequestId());
            }
            Utils.getExecutorService().submit(() -> {
                int secondPassed = 0;
                while (maxSecond[0] > 0 && !completableFuture.isDone()) {
                    try {
                        DescribeSpotInstanceRequestsResponse describeResponses = client
                            .describeSpotInstanceRequests(DescribeSpotInstanceRequestsRequest.builder()
                                .spotInstanceRequestIds(spotInstanceRequestIds).build());
                        for (SpotInstanceRequest describeResponse : describeResponses.spotInstanceRequests()) {
                            log.debug("{}", describeResponse.status());
                            if (describeResponse.status().code().equalsIgnoreCase("price-too-low") || describeResponse.state() == SpotInstanceState.FAILED || describeResponse.state() == SpotInstanceState.ACTIVE) {
                                if (describeResponse.state() == SpotInstanceState.FAILED || describeResponse.status().code().equalsIgnoreCase("price-too-low")) {
                                    completableFuture.completeExceptionally(new Exception(describeResponse.fault().message()));
                                    return;
                                }
                                instance = client.describeInstances(c -> c.instanceIds(describeResponse.instanceId())).reservations().get(0).instances().get(0);
                                if (instance.publicIpAddress() != null) {
                                    completableFuture.complete(describeResponse);
                                    log.debug("Current Spot Instance: {}", describeResponse.instanceId());
                                    log.debug("Created Spot Instance At: {}", describeResponse.createTime());
                                    log.debug("Found Spot Instance for bid:{}  quote:{}", describeResponse.actualBlockHourlyPrice(), selectedInstance.getValue());
                                    log.debug("Instance Public IP: {}", instance.publicIpAddress());
                                    return;
                                } else if (secondPassed > 30) {
                                    log.debug("Seconds exceeeds");
                                }
                            }
                        }
                        Thread.sleep(1000);
                        secondPassed++;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    maxSecond[0]--;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            completableFuture.completeExceptionally(e);
        }
        return completableFuture;

    }

    public void terminateInstance(Instance instance) {
        Ec2Client client = client();
        int wait = 60;
        while (wait > 0) {
            TerminateInstancesResponse terminateInstancesResponse = client.terminateInstances(c -> c.instanceIds(instance.instanceId()));
            InstanceState instanceState = terminateInstancesResponse.terminatingInstances().get(0).currentState();
            log.debug("State: {}", instanceState.name());
            if (instanceState.code() == 48) {
                break;
            }
            wait--;
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void cancelProvision() {
        Ec2Client client = client();
        if (!spotInstanceRequestIds.isEmpty()) {
            client.cancelSpotInstanceRequests(CancelSpotInstanceRequestsRequest.builder().spotInstanceRequestIds(spotInstanceRequestIds).build());
        }
        if (instance != null) {
            log.debug("Terminating: {}", instance.instanceId());
            terminateInstance(instance);
        }
        if (keyPairName != null) {
            try {
                log.debug("Deleting Key Pair: {}", keyPairName);
                FileUtils.cleanDirectory(Utils.getSshDir());
            } catch (IOException e) {
                e.printStackTrace();
            }
            client.deleteKeyPair(e -> e.keyName(keyPairName));
        }
        if (securityGroupId != null) {
            log.debug("Deleting Security Group: {}", securityGroupId);
            client.deleteSecurityGroup(e -> e.groupId(securityGroupId));
        }
        Utils.getExecutorService().shutdown();
    }
}
