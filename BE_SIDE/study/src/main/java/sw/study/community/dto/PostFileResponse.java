package sw.study.community.dto;

import lombok.Data;

@Data
public class PostFileResponse {
    String url;

    public PostFileResponse(String url) {
        this.url = url;
    }
}
