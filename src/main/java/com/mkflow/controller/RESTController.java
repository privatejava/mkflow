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

package com.mkflow.controller;


import com.mkflow.mapper.CodebaseMapper;
import com.mkflow.model.LogMessage;
import com.mkflow.model.Server;
import com.mkflow.service.AWSService;
import com.mkflow.service.HookHandlerService;
import com.mkflow.service.JobQueueService;
import io.vertx.core.http.HttpServerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
	CodebaseMapper codebaseMapper;

	@Inject
	JobQueueService jobQueueService;

	@Inject
	HookHandlerService hookHandlerService;

	@Inject
	AWSService awsService;


	@GET
	public String hello(){
		log.debug("Properties: {}",System.getProperties());
		log.debug("Env: {}",System.getenv());
		return "This is API for Mkflow";
	}

	/**
	 * This endpoint is for listing out all the Jobs available in the container
	 * @return
	 * @throws Exception
	 */
	@GET
	@Path("jobs")
	public Set<String> jobs() throws Exception {
		return jobQueueService.getJobs().keySet();
	}

	/**
	 * Helps to fetch the logs by filtering the time and limit of rows.
	 *
	 * @param params
	 * @return
	 * @throws Exception
	 */
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
				if(!path.toFile().exists()){
					log.debug("Using Cloudwatch");
					return awsService.getAWSCloudwatchResult((List<Map>)params.get("lastLines"),jobId, start).get();
				}
				Integer line = params.containsKey("line") ? Integer.parseInt(params.get("line").toString()) : 0;
				try (Stream<String> lines = Files.lines(path)) {
					return lines.skip(line).limit(500).map(l -> {
						Pattern compile = Pattern.compile("\\[([0-9a-z\\-]+):([0-9a-z\\-]+)\\]:(.*)");
						Matcher matcher = compile.matcher(l);
						if (matcher.matches() && matcher.groupCount() >= 3) {
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
			} else {
				log.debug("Using Cloudwatch");
				return awsService.getAWSCloudwatchResult((List<Map>)params.get("lastLines"),jobId, start).get();
			}

		}
		return empty;

	}




	/**
	 * Directly run a task in a machine without any source reposistory. This is more likely to be used for batch jobs.
	 * @param dto
	 * @return
	 * @throws Exception
	 */
	@Path("run")
	@POST
	public Map<String, String> runOnly(Map dto) throws Exception {
		dto.put("type","task");
		return awsService.processForLambda(dto);

	}

	@Path("debug")
	@POST
	public String debug(Map body){
		log.debug("Body: {}", body);
		log.debug("Request Headers: {}", request.headers());
		log.debug("Request params: {}", request.params());
		log.debug("Request query: {}", request.query());
		log.debug("Request method: {}", request.method());
		log.debug("Request path: {}", request.path());
		log.debug("Other: {}", request);
		return "ok";
	}


	/**
	 * This endpoint is not normally called but it is instead called by another rest endpoint for making the
	 * serverless application to call asynchronously. It is a prodcedure in serverless architecture because of 30 sec
	 * limit on AWS API Gateway.
	 *
	 * @param json
	 * @return
	 * @throws Exception
	 */
	@Path("run-direct")
	@POST
	public Map<String, String> runDirect(Map json) throws Exception {
		String key = null;
		if(json.containsKey("uniqueKey")){
			key = json.get("uniqueKey").toString();
			json.remove("uniqueKey");
		}
		return hookHandlerService.process(key,json,false);
	}

	/**
	 * Web Hooks for getting an update for running build from Githu or Gogs repository.
	 * @param json
	 * @return
	 * @throws Exception
	 */
	@Path("hook")
	@POST
	public Map<String, Object> hook(Map json) throws Exception {
		if (request.getHeader("x-github-event") != null && request.getParam("token") != null) {
			String token = request.getParam("token");
			json.put("token", token);
			json.put("type", "github");
		} else if (request.getHeader("x-gogs-event") != null && request.getParam("user") != null && request.getParam("pass") != null) {
			json.put("type", "gogs");
			json.put("user", request.getParam("user"));
			json.put("pass", request.getParam("pass"));
		}
		return awsService.processForLambda(json);
	}



}
