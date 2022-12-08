package exceptions;

public class IncorrectLoginNameException extends Throwable{
    @Override
    public String getMessage() {
        return "Login can not include special characters or spaces";
    }
}
