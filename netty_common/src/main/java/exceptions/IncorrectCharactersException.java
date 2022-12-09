package exceptions;

public class IncorrectCharactersException extends Throwable{
    @Override
    public String getMessage() {
        return "Only letters and numbers are allowed";
    }
}
