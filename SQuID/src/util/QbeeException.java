package util;

/**
 * An exception indicating some internal QueryPredict problem
 */
public class QbeeException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public QbeeException() {
        super();
    }

    public QbeeException(String message) {
        super(message);
    }

    QbeeException(Throwable cause) {
        super(cause);
    }
}
