package exceptions;

public class WrongPasswordException extends Throwable{
    @Override
    public String getMessage() {
        return "Current password is incorrect";
    }
}
