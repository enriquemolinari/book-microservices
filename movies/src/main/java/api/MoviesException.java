package api;

public class MoviesException extends RuntimeException {

	public MoviesException(String msg, Exception e) {
		super(msg, e);
	}

	public MoviesException(String msg) {
		super(msg);
	}
}
