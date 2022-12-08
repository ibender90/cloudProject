package exceptions;

public class LoginSizeNotInLimitsException extends Throwable {
    @Override
    public String getMessage() {
        return "Login must be 3-12 characters";
    }
}
