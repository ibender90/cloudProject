package exceptions;

public class IncorrectFolderNameException extends Exception{

    @Override
    public String getMessage() {
        return "Choose different name";
    }
}
