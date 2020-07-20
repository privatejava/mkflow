package com.mkflow.model;

import java.util.List;
import java.util.Map;


public class Buildspec {
    private Phase install;

    private String runAs;

    private List<Map<String, String>> environments;

    private Phase preBuild;

    private Phase build;

    private Phase postBuild;

    private CachePath cache;

    private Artifact artifacts;

    public Buildspec() {
    }


    public Phase getInstall() {
        return install;
    }

    public void setInstall(Phase install) {
        this.install = install;
    }

    public String getRunAs() {
        return runAs;
    }

    public void setRunAs(String runAs) {
        this.runAs = runAs;
    }

    public List<Map<String, String>> getEnvironments() {
        return environments;
    }

    public void setEnvironments(List<Map<String, String>> environments) {
        this.environments = environments;
    }

    public Phase getPreBuild() {
        return preBuild;
    }

    public void setPreBuild(Phase preBuild) {
        this.preBuild = preBuild;
    }

    public Phase getBuild() {
        return build;
    }

    public void setBuild(Phase build) {
        this.build = build;
    }

    public Phase getPostBuild() {
        return postBuild;
    }

    public void setPostBuild(Phase postBuild) {
        this.postBuild = postBuild;
    }

    public CachePath getCache() {
        return cache;
    }

    public void setCache(CachePath cache) {
        this.cache = cache;
    }

    public Artifact getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(Artifact artifacts) {
        this.artifacts = artifacts;
    }

    @Override
    public String toString() {
        return "Buildspec{" +
            "install=" + install +
            ", runAs='" + runAs + '\'' +
            ", environments=" + environments +
            ", preBuild=" + preBuild +
            ", build=" + build +
            ", postBuild=" + postBuild +
            '}';
    }
}
