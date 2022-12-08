package exceptions;

import java.io.IOException;

public class FolderExistsException extends IOException {

    @Override
    public String getMessage() {
        return "Folder already exists";
    }
}
