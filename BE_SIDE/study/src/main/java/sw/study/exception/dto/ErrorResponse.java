package sw.study.exception.dto;

import lombok.Data;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;

@Data
public class ErrorResponse implements org.springframework.web.ErrorResponse {
    private final String message;

    public ErrorResponse(String message) {
        this.message = message;
    }

    @Override
    public HttpStatusCode getStatusCode() {
        return null;
    }

    @Override
    public ProblemDetail getBody() {
        return null;
    }
}
