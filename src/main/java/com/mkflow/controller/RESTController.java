package com.mkflow.controller;

import com.amazonaws.services.logs.AWSLogs;
import com.amazonaws.services.logs.AWSLogsClient;
import com.amazonaws.services.logs.model.GetQueryResultsRequest;
import com.amazonaws.services.logs.model.GetQueryResultsResult;
import com.amazonaws.services.logs.model.ResultField;
import com.amazonaws.services.logs.model.StartQueryRequest;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.mkflow.dto.RunServerDTO;
import com.mkflow.mapper.CodebaseMapper;
import com.mkflow.model.*;
import com.mkflow.model.auth.AWSBasicAuthentication;
import com.mkflow.model.auth.Authentication;
import com.mkflow.model.auth.AuthenticationMethod;
import com.mkflow.model.aws.AWSServer;
import com.mkflow.service.JobQueueService;
import com.mkflow.utils.Utils;
import io.vertx.core.http.HttpServerRequest;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Path("api")
@Consumes("application/json")
@Produces("application/json")
public class RESTController {

    private static final Logger log = LoggerFactory.getLogger(RESTController.class);

    @Context
    HttpServerRequest request;

    @Inject
    private CodebaseMapper codebaseMapper;

    @Inject
    private JobQueueService jobQueueService;

    @Path("log")
    @POST
    public List<LogMessage> logs(Map params) throws Exception {
        List empty = new ArrayList();
        log.debug("{}", params);
        if (params.containsKey("jobId")) {

            Long start = params.containsKey("from") ? Long.parseLong(params.get("from").toString()) : Instant.EPOCH.getEpochSecond();
            String jobId = (String) params.get("jobId");
            log.debug("Jobs: {}", jobQueueService.getJobs());
            Server server = jobQueueService.getJob(jobId);
            if (server != null) {
                log.debug("Using File");
                java.nio.file.Path path = server.getOutputFile().toPath();
                Integer line = params.containsKey("line") ? Integer.parseInt(params.get("line").toString()) : 0;
                try (Stream<String> lines = Files.lines(path)) {
                    return lines.skip(line).limit(500).map(l->{
                        Pattern compile = Pattern.compile("\\[(.*):(.*)\\]:(.*)");
                        Matcher matcher = compile.matcher(l);
                        if(matcher.matches()){
                            String uniqueId = matcher.group(1);
                            Long time = Long.parseLong(matcher.group(2));
                            LogMessage message = new LogMessage();
                            message.setTime(time);
                            message.setMessage(matcher.group(3));
                            return message;
                        }
                        return null;
                    }).collect(Collectors.toList());
                }
            }else{
                log.debug("Using Cloudwatch");
                return getResult(jobId,start).get();
            }

        }
        return empty;

    }

    private CompletableFuture<List<LogMessage>> getResult(String uniqueId, Long from){
        CompletableFuture<List<LogMessage>> completeFuture = new CompletableFuture<>();
        Utils.getExecutorService().submit(()->{
            AWSLogs client = AWSLogsClient.builder().build();
            StartQueryRequest request= new StartQueryRequest();
            request.setLimit(20);
            request.setLogGroupName("/aws/lambda/lead-api-staging-leadApi");
            request.setStartTime(from);
            request.setEndTime(new Date().getTime()/1000L);
            request.setQueryString("fields @timestamp, @message\n" +
                "                       | sort @timestamp desc");
            GetQueryResultsResult queryRes = null;
            String queryId = client.startQuery(request).getQueryId();
            while(queryRes==null || queryRes.getStatus().equalsIgnoreCase("running")){
                GetQueryResultsRequest req2 = new GetQueryResultsRequest();
                req2.setQueryId(queryId);
                queryRes = client.getQueryResults(req2);
                log.debug("{}:{}", req2,queryRes.getStatus() );
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            completeFuture.complete(queryRes.getResults().stream().map(m-> {
                Optional<ResultField> msg = m.stream().filter(f -> f.getField().equalsIgnoreCase("@message")).findFirst();
                Optional<ResultField> time = m.stream().filter(f -> f.getField().equalsIgnoreCase("@timestamp")).findFirst();
                if(msg.isPresent() && time.isPresent()){
                    LogMessage message = new LogMessage();
                    message.setMessage(msg.get().getValue());
                    try {
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss.SSS");
                        message.setTime(format.parse(time.get().getValue()).getTime()/1000);
                        return message;
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toList()));
        });
        return completeFuture;
    }

    @Path("run")
    @POST
    public Map<String, String> run(RunServerDTO dto) throws Exception {
        Server server = ServerUtils.create(dto.getType(), dto.getCommands(), dto.getParams().getAuthMethod(),
            dto.getParams().getDetail().getUsername(), dto.getParams().getDetail().getPassword(), dto.getProvisionType(), codebaseMapper.fromDTO(dto.getCodebase()));
        Map<String, String> result = new HashMap<>();
        result.put("jobId", jobQueueService.addJob(server));
        return result;
    }

    @Path("hook")
    @POST
    public Map<String, String> hook(Map json) throws Exception {
        Map resp = new HashMap();
        if (request.getHeader("x-github-event") != null && request.getParam("token") != null) {
            resp.put("github", "true");
            String token = request.getParam("token");
            DocumentContext doc = JsonPath.parse(json);
            String url = doc.read("$.repository.url", String.class);
            String branch = doc.read("$.ref", String.class);
            resp.put("url", url);
            java.nio.file.Path test = Files.createTempDirectory("test");
            CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(token, "");
            Git git = Git.cloneRepository()
                .setURI(url)
                .setDirectory(test.toFile())
                .setBranchesToClone(Arrays.asList(branch))
                .setCredentialsProvider(credentialsProvider)
                .call();

            processGit(test,resp);

            resp.put("dir", test.toString());
        } else if (request.getHeader("x-gogs-event") != null && request.getParam("user") != null && request.getParam("pass") != null){
            resp.put("gogs", "true");
            String user = request.getParam("user");
            String pass = request.getParam("pass");
            DocumentContext doc = JsonPath.parse(json);
            String url = doc.read("$.repository.html_url", String.class);
            String branch = doc.read("$.ref", String.class);
            resp.put("url", url);
            java.nio.file.Path test = Files.createTempDirectory("test");
            log.debug("{}", test);
            CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(user, pass);
            Git git = Git.cloneRepository()
                .setURI(url)
                .setDirectory(test.toFile())
                .setBranchesToClone(Arrays.asList(branch))
                .setCredentialsProvider(credentialsProvider)
                .call();
            processGit(test,resp);
        } else{
            resp.put("github", "false");
        }
        return resp;
    }

    private void processGit(java.nio.file.Path test,Map resp) throws IOException {
        File mkFlowFile = Utils.findBobFlowFile(test.toFile());
        if (mkFlowFile != null) {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            resp.put("file", mkFlowFile.getAbsolutePath());
            Map map = mapper.readValue(mkFlowFile, Map.class);
            SimpleModule module = new SimpleModule("CustomModel", Version.unknownVersion());
            SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
            DocumentContext parse = JsonPath.parse(map);
            if (map != null && map.containsKey("cloud")) {
                CloudVendor vendor = CloudVendor.parse(((Map) map.get("cloud")).get("vendor").toString());
                Server server = null;
                switch (vendor) {
                    case AMAZON:
                        if(parse.read("$.cloud.auth") != null && parse.read("$.cloud.auth.type")!= null){
                            AuthenticationMethod method = AuthenticationMethod.parse(parse.read("$.cloud.auth.type"));
                            switch (method){
                                case PARAMS:
                                    log.debug("Auth type: {}", method);
                                    resolver.addMapping(Authentication.class, AWSBasicAuthentication.class);
                                    break;
                            }
                        }

                        resolver.addMapping(IAMPermission.class, AWSPermission.class);
                        module.setAbstractTypes(resolver);
                        mapper.registerModule(module);
                        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
                        server = mapper.readValue(mkFlowFile, AWSServer.class);
                        break;
                }
                server.init();
                FileUtils.copyDirectory(test.toFile(), server.getSourceFile());

                File zipFile = server.getWorkDir().resolve("codebase.zip").toFile();
                log.debug("{} -> {}", test.resolve("src").toString(), zipFile.getAbsolutePath());
                Utils.zip(test.resolve("src").toString(), zipFile.getAbsolutePath());

                log.debug("{}", server);
                log.debug("Perm: {}", server.getCloud().getProvision().getPermission());
                resp.put("jobId", jobQueueService.addJob(server));
                log.debug("Jobs: {}", jobQueueService.getJobs());
                resp.put("codebase", server.getSourceFile().getAbsolutePath());
            }
        }
    }
}
