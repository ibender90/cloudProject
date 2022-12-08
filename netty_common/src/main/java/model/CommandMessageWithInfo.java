package model;

import enums.Commands;

import java.util.Arrays;

public class CommandMessageWithInfo extends AbstractMessage{
    private Commands command;
    private String[] content; //login & pass | id | error

    private String path;

    private String[] filesArray;


    public CommandMessageWithInfo(Commands command){
        this.command = command;
    };
    public CommandMessageWithInfo(Commands command, String[] content) {
        this.command = command;
        this.content = content;
    }

    public CommandMessageWithInfo(Commands command, String path) {
        this.command = command;
        this.path = path;
    }

    public Commands getCommand() {
        return command;
    }

    public void setCommand(Commands command) {
        this.command = command;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String[] getFilesArray() {
        return filesArray;
    }

    public void setFilesArray(String[] filesArray) {
        this.filesArray = filesArray;
    }

    public String[] getContent() {
        return content;
    }

    public void setContent(String[] content) {
        this.content = content;
    }


    @Override
    public String toString() {
        return "CommandMessageWithInfo{" +
                "command=" + command +
                ", content=" + Arrays.toString(content) +
                ", path='" + path + '\'' +
                ", filesArray=" + Arrays.toString(filesArray) +
                '}';
    }
}
