package com.mkflow.service;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.mkflow.controller.LambdaController;
import com.mkflow.model.*;
import com.mkflow.model.auth.AWSBasicAuthentication;
import com.mkflow.model.auth.Authentication;
import com.mkflow.model.aws.AWSServer;
import com.mkflow.utils.Utils;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Narayan <me@ngopal.com.np> - ngm
 * Created 20/07/2020 17:21
 **/
@ApplicationScoped
public class HookHandlerService {
	private static Logger log = LoggerFactory.getLogger(HookHandlerService.class);

	@Inject
	private JobQueueService jobQueueService;


	public Map process(Map json) throws IOException, GitAPIException {
		String type = json.containsKey("type")? json.get("type").toString():null;
		String token = json.containsKey("token")? json.get("token").toString():null;
		String user = json.containsKey("user")? json.get("user").toString():null;
		String pass = json.containsKey("pass")? json.get("pass").toString():null;

		Map resp = new HashMap();
		if (type.equalsIgnoreCase("github")) {
			resp.put("github", "true");
			String url = json.containsKey("repository") && ((Map) json.get("repository")).containsKey("url") ?
					((Map) json.get("repository")).get("url").toString() : null;
			String branch = json.containsKey("ref") ? json.get("ref").toString() : null;
			resp.put("url", url);
			java.nio.file.Path test = Files.createTempDirectory("test");

			CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(token, "");
			Git.cloneRepository()
					.setURI(url)
					.setDirectory(test.toFile())
					.setBranchesToClone(Arrays.asList(branch))
					.setCredentialsProvider(credentialsProvider)
					.call();
			processGit(test,resp);
			resp.put("dir", test.toString());
		} else if (type.equalsIgnoreCase("gogs")) {
			resp.put("gogs", "true");
			String url = json.containsKey("repository") && ((Map) json.get("repository"))
					.containsKey("html_url") ? ((Map) json.get("repository")).get("html_url").toString() : null;
			String branch = json.containsKey("ref") ? json.get("ref").toString() : null;
			resp.put("url", url);
			java.nio.file.Path test = Files.createTempDirectory("test");
			log.debug("{}", test);
			CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(user, pass);
			Git.cloneRepository()
					.setURI(url)
					.setDirectory(test.toFile())
					.setBranchesToClone(Arrays.asList(branch))
					.setCredentialsProvider(credentialsProvider)
					.call();
			processGit(test,resp);
		} else {
			resp.put("github", "false");
		}
		return resp;
	}

	private void processGit(java.nio.file.Path test, Map resp) throws IOException {
		File mkFlowFile = Utils.findBobFlowFile(test.toFile());
		if (mkFlowFile != null) {
			ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
			resp.put("file", mkFlowFile.getAbsolutePath());
			Map map = mapper.readValue(mkFlowFile, Map.class);
			SimpleModule module = new SimpleModule("CustomModel", Version.unknownVersion());
			SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
			if (map != null && map.containsKey("cloud")) {
				CloudVendor vendor = CloudVendor.parse(Utils.getByPath(map,"cloud.vendor",String.class));
				Server server = null;
				switch (vendor) {
					case AMAZON:
						if (Utils.getByPath(map,"cloud.auth",Map.class) !=null &&
								Utils.getByPath(map,"cloud.auth.type",String.class) !=null) {
							AuthenticationMethod method = AuthenticationMethod.parse(Utils.getByPath(map,"cloud.auth.type",String.class));
							switch (method) {
								case USER_PASS:
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
