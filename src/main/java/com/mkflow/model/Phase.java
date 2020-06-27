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
