package sw.study.exception.community;

public class CommentNotBelongToPostException extends RuntimeException {
    public CommentNotBelongToPostException(String message) {
        super(message);
    }
}
