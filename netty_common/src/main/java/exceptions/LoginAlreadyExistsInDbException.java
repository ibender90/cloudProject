package exceptions;

import java.sql.SQLException;

public class LoginAlreadyExistsInDbException extends Exception {
    @Override
    public String getMessage() {
        return "This login is already in use";
    }
}
