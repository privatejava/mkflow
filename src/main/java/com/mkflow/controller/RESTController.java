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


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mkflow.dto.RunServerDTO;
import com.mkflow.mapper.CodebaseMapper;
import com.mkflow.model.LambdaRequestModel;
import com.mkflow.model.LogMessage;
import com.mkflow.model.Server;
import com.mkflow.model.ServerUtils;
import com.mkflow.service.HookHandlerService;
import com.mkflow.service.JobQueueService;
import com.mkflow.utils.Utils;
import io.vertx.core.http.HttpServerRequest;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.core.SdkField;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.*;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.*;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
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
	CodebaseMapper codebaseMapper;

	@Inject
	JobQueueService jobQueueService;

	@Inject
	ObjectMapper mapper;

	@Inject
	HookHandlerService hookHandlerService;


	@GET
	public String hello(){
		log.debug("Properties: {}",System.getProperties());
		log.debug("Env: {}",System.getenv());
		return "This is API for Mkflow";
	}

	@GET
	@Path("jobs")
	public Set<String> jobs() throws Exception {
		return jobQueueService.getJobs().keySet();
	}

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
					return lines.skip(line).limit(500).map(l -> {
						Pattern compile = Pattern.compile("\\[(.*):(.*)\\]:(.*)");
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
				return getResult(jobId, start).get();
			}

		}
		return empty;

	}


	private CompletableFuture<List<LogMessage>> getResult(String uniqueId, Long from) {
		CompletableFuture<List<LogMessage>> completeFuture = new CompletableFuture<>();
        Utils.getExecutorService().submit(()-> {
	        CloudWatchLogsClient client = CloudWatchLogsClient.builder().region(Region.AP_SOUTHEAST_1).httpClient(
			        UrlConnectionHttpClient.builder().build()
	        ).build();

            StartQueryRequest request= StartQueryRequest.builder().limit(20)
		            .logGroupName("/aws/lambda/mkflow-staging-api")
		            .startTime(from)
		            .endTime(new Date().getTime()/1000L)
		            .queryString("fields @timestamp, @message\n" +
				            "                       | sort @timestamp desc").build();
	        StartQueryResponse startQueryResponse = client.startQuery(request);
            GetQueryResultsResponse queryRes = null;
            String queryId = client.startQuery(request).queryId();
            while(queryRes==null || queryRes.status().equals(QueryStatus.RUNNING)){
                queryRes = client.getQueryResults(r->r.queryId(queryId));
                log.debug("{}:{}", queryRes.status() );
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            completeFuture.complete(queryRes.results().stream().map(m-> {
                Optional<ResultField> msg = m.stream().filter(f -> f.field().equalsIgnoreCase("@message")).findFirst();
                Optional<ResultField> time = m.stream().filter(f -> f.field().equalsIgnoreCase("@timestamp")).findFirst();
                if(msg.isPresent() && time.isPresent()){
                    LogMessage message = new LogMessage();
                    message.setMessage(msg.get().value());
                    try {
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss.SSS");
                        message.setTime(format.parse(time.get().value()).getTime()/1000);
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
	public Map<String, String> runOnly(Map dto) throws Exception {
		dto.put("type","task");
		return hookHandlerService.process(dto,true);
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


	@Path("run-direct")
	@POST
	public Map<String, String> runDirect(Map json) throws Exception {
		return processForLambda(json);
	}

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
		return processForLambda(json);
	}

	private Map processForLambda(Map json) throws IOException, GitAPIException {
		//Only applies for the lambda
		if(System.getenv("DISABLE_SIGNAL_HANDLERS") != null){
			Region region = Region.AP_SOUTHEAST_1;
			LambdaRequestModel model = new LambdaRequestModel();
			model.setPath("/api/run-direct");
			model.addMultiValueHeader("content-type", Arrays.asList("application/json"));
			model.setHttpMethod("POST");
			model.setBody(mapper.writeValueAsString(json));
			LambdaClient awsLambda = LambdaClient.builder().region(region)
					.httpClient(software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient.builder().build())
					.build();
			SdkBytes payload = SdkBytes.fromUtf8String(mapper.writeValueAsString(model));
			InvokeRequest request = InvokeRequest.builder()
					.logType(LogType.TAIL)
					.functionName(System.getenv("LAMBDA_NAME")!=null?System.getenv("LAMBDA_NAME"):
							"mkflow-staging-api")
					.invocationType(InvocationType.EVENT)
					.payload(payload)
					.build();
			//Invoke the Lambda function
			InvokeResponse res= awsLambda.invoke(request);
			return new HashMap<>();
		}else{
			return hookHandlerService.process(json,true);
		}
	}

}
