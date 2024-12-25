package sw.study.community.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class PostResponse {
    private Long id;
    private String title;
    private String content;
    private int likeCount;
    private int viewCount;
    private int commentCount;
    private LocalDateTime createdAt;

    private PostAuthorResponse postAuthor;
}
