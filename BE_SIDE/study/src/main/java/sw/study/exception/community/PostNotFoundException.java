package sw.study.exception.community;

public class PostNotFoundException  extends RuntimeException{
    public PostNotFoundException(String message) {
        super(message);
    }
}
