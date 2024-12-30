package api;

public class UsersException extends RuntimeException {

    public UsersException(String msg, Exception e) {
        super(msg, e);
    }

    public UsersException(String msg) {
        super(msg);
    }
}
