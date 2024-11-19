package sw.study.community.dto;

import lombok.Data;

@Data
public class CommentRequest {
    private Long memberId;
    private String content;
    private int level;
}
