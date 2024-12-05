package sw.study.community.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CommentResponse {
    private Long commentId;
    private String content;
    private int likeCount;
    private int level;
    private boolean isDeleted;

    private CommentAuthorResponse commentAuthorResponse;
    private List<CommentResponse> child = new ArrayList<>();
}
