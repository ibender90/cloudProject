package exceptions;

public class FileExistsException extends Throwable{

    @Override
    public String getMessage() {
        return "File with this name already exists";
    }
}
