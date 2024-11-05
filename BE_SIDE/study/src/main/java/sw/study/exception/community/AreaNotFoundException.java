package sw.study.exception.community;

public class AreaNotFoundException extends RuntimeException {
    public AreaNotFoundException(String message) {
        super(message);
    }
}
