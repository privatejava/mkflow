package com.mkflow.controller;

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
import com.mkflow.model.aws.AWSServer;
import com.mkflow.service.JobQueueService;
import com.mkflow.utils.Utils;
import io.vertx.core.http.HttpServerRequest;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Path("api")
@Consumes("application/json")
@Produces("application/json")
public class RESTController {

    private static final Logger log = LogManager.getLogger(RESTController.class);

    @Context
    HttpServerRequest request;

    @Inject
    private CodebaseMapper codebaseMapper;

    @Inject
    private JobQueueService jobQueueService;

    @Path("log")
    @POST
    public List<String> logs(Map params) throws Exception {
        List empty = new ArrayList();
        log.debug("{}", params);
        if (params.containsKey("jobId")) {
            log.debug("Jobs: {}", jobQueueService.getJobs());
            Server server = jobQueueService.getJob((String) params.get("jobId"));
            if (server != null) {
                java.nio.file.Path path = server.getOutputFile().toPath();
                Integer line = params.containsKey("line") ? Integer.parseInt(params.get("line").toString()) : 0;
                try (Stream<String> lines = Files.lines(path)) {
                    return lines.skip(line).limit(500).collect(Collectors.toList());
                }
            }

        }
        return empty;

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

            File mkFlowFile = Utils.findBobFlowFile(test.toFile());
            if (mkFlowFile != null) {
                ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
                resp.put("file", mkFlowFile.getAbsolutePath());
                Map map = mapper.readValue(mkFlowFile, Map.class);
                if (map != null && map.containsKey("cloud")) {
                    CloudVendor vendor = CloudVendor.parse(((Map) map.get("cloud")).get("vendor").toString());
                    Server server = null;
                    switch (vendor) {
                        case AMAZON:
                            SimpleModule module = new SimpleModule("CustomModel", Version.unknownVersion());
                            SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
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

            resp.put("dir", test.toString());
        } else {
            resp.put("github", "false");
        }
        return resp;
    }
}
