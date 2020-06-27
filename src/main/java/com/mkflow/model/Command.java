package com.mkflow.model;


public class Command {
    private String shell;

    private String user;

    private String command;

    public Command(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    @Override
    public String toString() {
        return "Command{" +
            "command='" + command + '\'' +
            '}';
    }
}
