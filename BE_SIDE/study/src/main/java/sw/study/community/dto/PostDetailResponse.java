package sw.study.community.dto;


import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PostDetailResponse {
    private Long postId;
    private String title;
    private String content;
    private String category;
    private int viewCount;
    private int reportCount;
    private int likeCount;

    private PostAuthorResponse postAuthorResponse;

    private List<PostFileResponse> filesResponse = new ArrayList<>();
    private List<PostAreaResponse> interestsResponse = new ArrayList<>();
    private List<CommentResponse> commentsResponse = new ArrayList<>();
}