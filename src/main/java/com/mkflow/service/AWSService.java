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

package com.mkflow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mkflow.controller.RESTController;
import com.mkflow.model.LambdaRequestModel;
import com.mkflow.model.LogMessage;
import com.mkflow.utils.Utils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.*;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvocationType;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;
import software.amazon.awssdk.services.lambda.model.LogType;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Narayan <me@ngopal.com.np> - ngm
 * Created 23/07/2020 01:10
 **/
@ApplicationScoped
public class AWSService {
	private static final Logger log = LoggerFactory.getLogger(AWSService.class);

	@Inject
	ObjectMapper mapper;

	@Inject
	HookHandlerService hookHandlerService;

	/**
	 * This invokes the actual AWS Lambda function for doing asynchronous request.
	 *
	 * @param json
	 * @return
	 * @throws IOException
	 * @throws GitAPIException
	 */
	public Map processForLambda(Map json) throws IOException, GitAPIException {
		//Only applies for the lambda
		if(System.getenv("DISABLE_SIGNAL_HANDLERS") != null){
			log.debug("Using Lambda Invoke");
			Region region = Region.AP_SOUTHEAST_1;
			LambdaRequestModel model = new LambdaRequestModel();
			model.setPath("/api/run-direct");
			model.addMultiValueHeader("content-type", Arrays.asList("application/json"));
			model.setHttpMethod("POST");
			String uniqueId = UUID.randomUUID().toString();
			json.put("uniqueKey",uniqueId);
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
			HashMap<Object, Object> response = new HashMap<>();
			response.put("jobId",uniqueId);
			return response;
		}else{
			log.debug("Using Direct Invocation");
			return hookHandlerService.process(null,json,true);
		}
	}

	/**
	 * For now getting the AWS Cloudwatch logs result when there is no file logs. (fallback)
	 * @param uniqueId
	 * @param from
	 * @return
	 */
	public CompletableFuture<List<LogMessage>> getAWSCloudwatchResult(String uniqueId, Long from) {
		CompletableFuture<List<LogMessage>> completeFuture = new CompletableFuture<>();
		Utils.getExecutorService().submit(()-> {
			CloudWatchLogsClient client = CloudWatchLogsClient.builder().region(Region.AP_SOUTHEAST_1).httpClient(
					UrlConnectionHttpClient.builder().build()
			).build();

			log.debug("Time range {}:{} {}",from,new Date().getTime()/1000L,  (new Date().getTime()/1000L)-from);
			StartQueryRequest request= StartQueryRequest.builder().limit(20)
					.logGroupName("/aws/lambda/mkflow-staging-api")
					.startTime(from)
					.endTime(new Date().getTime()/1000L)
					.queryString("fields @timestamp, @message\n" +
							"                       | filter @message like '["+uniqueId+"'"+
							"                       | sort @timestamp asc").build();
			StartQueryResponse startQueryResponse = client.startQuery(request);
			GetQueryResultsResponse queryRes = null;
			String queryId = startQueryResponse.queryId();
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
				Pattern compile = Pattern.compile("\\[([0-9a-z\\-]+):([0-9a-z\\-]+)\\]:(.*)");

				if(msg.isPresent() && time.isPresent()){
					LogMessage message = new LogMessage();
					Matcher matcher = compile.matcher(msg.get().value());
					if (matcher.matches() && matcher.groupCount() >= 3) {
						message.setMessage(matcher.group(3));
					}else{
						message.setMessage(msg.get().value());
					}
					try {
						SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
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
}
