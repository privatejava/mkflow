package com.mkflow.model;

import java.util.List;

public class Build {
    private List<String> commands;

    private Repository repo;

    private BuildCache cache;

    public List<String> getCommands() {
        return commands;
    }

    public void setCommands(List<String> commands) {
        this.commands = commands;
    }

    public Repository getRepo() {
        return repo;
    }

    public void setRepo(Repository repo) {
        this.repo = repo;
    }

    public BuildCache getCache() {
        return cache;
    }

    public void setCache(BuildCache cache) {
        this.cache = cache;
    }
}
