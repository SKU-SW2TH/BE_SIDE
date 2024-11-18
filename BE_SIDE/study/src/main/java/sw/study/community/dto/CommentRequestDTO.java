package sw.study.community.dto;

import lombok.Data;

@Data
public class CommentRequestDTO {
    private Long memberId;
    private String content;
    private int level;
}
