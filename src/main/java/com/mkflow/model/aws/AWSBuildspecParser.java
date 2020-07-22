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

package com.mkflow.model.aws;


import com.mkflow.model.*;
import com.mkflow.utils.Utils;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

public class AWSBuildspecParser extends BuildSpecParser {
    private List<Command> getCommands(Object obj, String path) {

        List<String> read = Utils.getByPath(obj,path,List.class);
        if (read != null) {
            return read.stream().map(s -> new Command(s.toString())).collect(Collectors.toList());
        } else {
            return null;
        }

    }

    @Override
    public Buildspec parse(InputStream inputStream) {
        Buildspec buildspec = null;
        try {
            Object map = convertYamlToMap(IOUtils.toString(inputStream, "utf-8"));

//            DocumentContext doc = JsonPath.parse(json);
            List<Command> install = getCommands(map, "phases.install.commands");
            List<Command> pre = getCommands(map, "phases.pre_build.commands");
            List<Command> build = getCommands(map, "phases.build.commands");
            List<Command> post = getCommands(map, "phases.post_build.commands");
            CachePath cachePath = Utils.getByPath(map,"cache", CachePath.class);
            Artifact artifacts = Utils.getByPath(map,"artifacts", Artifact.class);
            if (install == null && pre == null && build == null && post == null) {
                return null;
            }
            buildspec = new Buildspec();
            buildspec.setInstall(new Phase(install));
            buildspec.setPreBuild(new Phase(pre));
            buildspec.setBuild(new Phase(build));
            buildspec.setPostBuild(new Phase(post));
            buildspec.setCache(cachePath);
            buildspec.setArtifacts(artifacts);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return buildspec;
    }
}
