package enums;

import java.util.Objects;

public enum Commands {
    MKDIR("@%pathForNewFolder"), DEL("@%PathToDelete"), AUTH("@%Login,pass"), DISCONNECT("@?"),
    AUTH_OK("@%ID"), ERROR("@%ErrorMessage"), PATHANDFILES("@%Path,FilesArray"), DOWNLOAD("@%PathToDownload"),
    CD("@%path"), INIT("@%GetPathByID"), REGISTER("@%Token,Login,Pass"), CHANGENICK("@%UserID,NewNick"),
    CHANGEPASS("@%UserID,OldPass,NewPass");

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
