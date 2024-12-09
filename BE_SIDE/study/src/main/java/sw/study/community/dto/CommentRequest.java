package sw.study.community.dto;

import lombok.Data;

@Data
public class CommentRequest {
    private String content;
    private int level;
}
