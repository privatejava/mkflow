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
