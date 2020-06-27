package com.mkflow.model;

public interface CacheProvider {
    void download(String source, String target) throws Exception;

    void upload(String source, String target) throws Exception;
}
