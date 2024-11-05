package sw.study.exception.email;

/**
 * 이메일 전송에 실패했을 때 발생하는 예외
 */
public class EmailSendException extends RuntimeException {
    public EmailSendException(String message, Throwable cause) {
        super(message, cause);
    }
}
