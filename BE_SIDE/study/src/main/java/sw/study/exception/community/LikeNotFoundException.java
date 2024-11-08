package sw.study.exception.community;

public class LikeNotFoundException extends RuntimeException {
    public LikeNotFoundException(String message) {
        super(message);
    }
}
