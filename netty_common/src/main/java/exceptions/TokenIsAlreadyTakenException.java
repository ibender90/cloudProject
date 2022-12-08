package exceptions;

public class TokenIsAlreadyTakenException extends Throwable{
    @Override
    public String getMessage() {
        return "This token belongs to another user";
    }
}
