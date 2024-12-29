package api;

public class ShowsException extends RuntimeException {

    public ShowsException(String msg, Exception e) {
        super(msg, e);
    }

    public ShowsException(String msg) {
        super(msg);
    }
}
