package sw.study.exception.email;

public class VerificationCodeGenerationException extends RuntimeException {
    public VerificationCodeGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}