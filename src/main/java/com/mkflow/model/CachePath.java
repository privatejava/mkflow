package com.mkflow.model;

import java.util.ArrayList;
import java.util.List;

public class CachePath {
    private List<String> paths;

    public List<String> getPaths() {
        return paths;
    }

    public void setPaths(List<String> paths) {
        this.paths = paths;
    }

    public void addPath(String path) {
        if (paths == null) {
            paths = new ArrayList<>();
        }
        paths.add(path);
    }
}
