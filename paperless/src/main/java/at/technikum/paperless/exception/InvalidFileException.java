package at.technikum.paperless.exception;

public class InvalidFileException extends ServiceException {

    public InvalidFileException(String message) {
        super(message);
    }
}