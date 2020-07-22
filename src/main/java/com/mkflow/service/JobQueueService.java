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

import com.mkflow.model.Server;
import com.mkflow.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@ApplicationScoped
public class JobQueueService {
	private static final Logger log = LoggerFactory.getLogger(JobQueueService.class);

	private Map<String, Server> jobs;

	public JobQueueService() {
		jobs = new HashMap<>();
	}

	public Map<String, Server> getJobs() {
		return Collections.unmodifiableMap(jobs);
	}

	public Server getJob(String jobId) {
		return jobs.get(jobId);
	}

	public String addJob(Server server, boolean async) {
		jobs.put(server.getUniqueId(), server);
		Runnable runnable = () -> {
			try {
				server.provision().get();
				Thread.sleep(30000);
				server.connect();
				log.debug("Executing the commands ");
				CompletableFuture<Boolean> execute = server.execute(false);
				Boolean b = execute.get();
				log.debug("Build Completed: {}", b);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (server != null) {
					server.cancelProvision();
				}
			}
		};
		if (async) {
			Utils.getExecutorService().submit(runnable);
		}else{
			runnable.run();
		}

		return server.getUniqueId();
	}
}
