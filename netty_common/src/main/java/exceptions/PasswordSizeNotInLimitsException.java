package exceptions;

public class PasswordSizeNotInLimitsException extends Throwable{
    @Override
    public String getMessage() {
        return "Password must be 3-12 characters";
    }
}
