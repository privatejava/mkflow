package com.mkflow.model.aws;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.mkflow.model.*;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

public class AWSBuildspecParser extends BuildSpecParser {
    private List<Command> getCommands(DocumentContext doc, String path) {
        try {
            List<String> read = doc.read(path, List.class);
            if (read != null) {
                return read.stream().map(s -> new Command(s.toString())).collect(Collectors.toList());
            } else {
                return null;
            }
        } catch (PathNotFoundException pnfe) {
            return null;
        }
    }

    @Override
    public Buildspec parse(InputStream inputStream) {
        Buildspec buildspec = null;
        try {
            String json = convertYamlToJson(IOUtils.toString(inputStream, "utf-8"));
            DocumentContext doc = JsonPath.parse(json);
            List<Command> install = getCommands(doc, "$.phases.install.commands");
            List<Command> pre = getCommands(doc, "$.phases.pre_build.commands");
            List<Command> build = getCommands(doc, "$.phases.build.commands");
            List<Command> post = getCommands(doc, "$.phases.post_build.commands");
            CachePath cachePath = doc.read("$.cache", CachePath.class);
            if (install == null && pre == null && build == null && post == null) {
                return null;
            }
            buildspec = new Buildspec();
            buildspec.setInstall(new Phase(install));
            buildspec.setPreBuild(new Phase(pre));
            buildspec.setBuild(new Phase(build));
            buildspec.setPostBuild(new Phase(post));
            buildspec.setCache(cachePath);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return buildspec;
    }
}
