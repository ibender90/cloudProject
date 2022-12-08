package exceptions;

public class InvalidTokenException extends Exception{
    @Override
    public String getMessage() {
        return "Token is incorrect";
    }
}
