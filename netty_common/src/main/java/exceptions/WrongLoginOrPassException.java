package exceptions;

import java.io.IOException;

public class WrongLoginOrPassException extends IOException {

    @Override
    public String getMessage() {
        return "Wrong login or password";
    }
}
