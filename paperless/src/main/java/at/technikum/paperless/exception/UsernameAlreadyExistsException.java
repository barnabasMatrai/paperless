package at.technikum.paperless.exception;

public class UsernameAlreadyExistsException extends ServiceException {

    public UsernameAlreadyExistsException() {
        super("Username already exists");
    }
}