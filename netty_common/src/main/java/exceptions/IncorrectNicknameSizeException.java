package exceptions;

public class IncorrectNicknameSizeException extends Throwable{

    @Override
    public String getMessage() {
        return "Nickname size is incorrect";
    }

}
