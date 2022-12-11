package exceptions;

public class ChooseAfileNotFolderException extends Throwable{
    @Override
    public String getMessage() {
        return "Choose a file, not folder";
    }
}
