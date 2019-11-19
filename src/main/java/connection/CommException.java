package connection;

public class CommException extends RuntimeException {

    public CommException(String message) {
        super(message);
    }

    public CommException(String message, Throwable cause) {
        super(message, cause);
    }
}
