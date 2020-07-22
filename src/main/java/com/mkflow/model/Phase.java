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

import java.util.ArrayList;
import java.util.List;

public class Phase {
    private List<Command> commands;

    public Phase() {

    }

    public Phase(List<Command> commands) {
        this.commands = commands;
    }

    public void addCommand(Command command) {
        if (this.commands == null) {
            this.commands = new ArrayList<>();
        }
        this.commands.add(command);
    }

    public List<Command> getCommands() {
        return commands;
    }

    public void setCommands(List<Command> commands) {
        this.commands = commands;
    }

    @Override
    public String toString() {
        return "Phase{" +
            "commands=" + commands +
            '}';
    }
}
