package exceptions;

public class PasswordMismatchException extends Throwable{
    @Override
    public String getMessage() {
        return "Passwords does not match";
    }
}
