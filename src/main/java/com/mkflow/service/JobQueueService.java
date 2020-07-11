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

    public String addJob(Server server) {
        jobs.put(server.getUniqueId(), server);
        Utils.getExecutorService().submit(() -> {
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
        });

        return server.getUniqueId();
    }
}
