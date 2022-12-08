package enums;

import java.util.Objects;

public enum Commands {
    MKDIR("@%mkdrir"), DEL("@%DELETE"), AUTH("@%auth"), DISCONNECT("@?DISCN"),
    AUTH_OK("@%AOK"), ERROR("@%ER"), PATHANDFILES("@%ALLOK"), DOWNLOAD("@%DWNLD"),
    CD("@%CHANGEDIR"), INIT("@%INITIALIZE"), REGISTER("@%REGISTERUSER"), CHANGENICK("@%NICKCHANGE");

    private String command;

    Commands(String c){
        this.command = c;
    }

    public static Commands getByCommand(String command) {
        for (Commands value : values()) {
            if (Objects.equals(value.command, command)) {
                return value;
            }
        }
        throw new IllegalArgumentException();
    }

    public String getCommand() {
        return command;
    }
}
