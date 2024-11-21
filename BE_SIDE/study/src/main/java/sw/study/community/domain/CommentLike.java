package sw.study.community.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sw.study.user.domain.Member;

import java.time.LocalDateTime;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class CommentLike {

    @Id
    @GeneratedValue
    @Column(name = "comment_like_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    //== 생성 메서드 ==//
    public static CommentLike createCommentLike(Comment comment, Member member) {
        CommentLike commentLike = new CommentLike();
        comment.addCommentLike(commentLike);
        commentLike.member = member;
        return commentLike;
    }

    public void addComment(Comment comment) {
        this.comment = comment;
    }
}
