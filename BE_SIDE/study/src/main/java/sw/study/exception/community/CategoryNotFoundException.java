package sw.study.exception.community;

// 해당하는 카테고리를 찾을 수 없을 때 발생하는 예외
public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(String message) {
        super(message);
    }
}
