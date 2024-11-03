package sw.study.exception.email;

public class VerificationCodeStorageException extends RuntimeException {
    public VerificationCodeStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}