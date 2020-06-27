package com.mkflow.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

public class BuildSpecParser {
    protected String convertYamlToJson(String yaml) throws JsonProcessingException {
        ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
        Object obj = yamlReader.readValue(yaml, Object.class);

        ObjectMapper jsonWriter = new ObjectMapper();
        return jsonWriter.writeValueAsString(obj);
    }

    public Buildspec parse(List<String> commands) throws YAMLException {
        Buildspec buildspec = new Buildspec();
        Phase phase = new Phase();
        phase.setCommands(commands.stream().map(m -> new Command(m)).collect(Collectors.toList()));
        buildspec.setBuild(phase);
        return buildspec;
    }

    public Buildspec parse(InputStream inputStream) throws YAMLException {
        Yaml yaml = new Yaml();
        Buildspec buildspec = yaml.loadAs(inputStream, Buildspec.class);
        return buildspec;
//        if (document.containsKey("commands") && document.get("commands") instanceof Collection) {
//            return (List<Command>) ((Collection) document.get("commands")).stream().map(e -> {
//                Command command = new Command(e.toString());
//                return command;
//            }).collect(Collectors.toList());
//        }
//        return null;
    }

    public Buildspec parse(String filePath) throws YAMLException {
        if (filePath.endsWith(".yml") || filePath.endsWith(".yaml")) {
            Yaml yaml = new Yaml();
            File path = new File(filePath);
            if (path.exists() && path.isFile()) {
                try {
                    return parse(new FileInputStream(path));
                } catch (IOException e) {
                    throw new IllegalArgumentException("Error : " + e.getMessage());
                }
            } else {
                throw new IllegalArgumentException("Cannot locate a file  : " + filePath);
            }
        }
        throw new IllegalArgumentException("Invalid extension provided . Only yaml files are supported.");

    }

}
