package sw.study.community.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sw.study.user.domain.Member;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static lombok.AccessLevel.*;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Comment {

    @Id @GeneratedValue
    @Column(name = "comment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @OneToMany(mappedBy = "parent")
    private List<Comment> child = new ArrayList<>();

    @OneToMany
    private List<CommentLike> commentLikes = new ArrayList<>();

    private String content;
    private int level;
    private int reportCount = 0;
    private boolean isDeleted = false;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    //== 생성 메서드 ==//
    public static Comment createComment(Post post, Member member, String content, int level) {
        Comment comment = new Comment();
        post.addComment(comment);
        comment.member = member;
        comment.content = content;
        comment.level = level;

        return comment;
    }

    public static Comment createReply(Comment parent, Member member, String content, int level) {
        Comment comment = new Comment();
        parent.addChildComment(comment);
        comment.member = member;
        comment.content = content;
        comment.level = level;

        return comment;
    }

    //== 연관 관계 편의 메서드 ==//
    public void addChildComment(Comment child) {
        this.child.add(child);
        child.addParentComment(this);
    }

    public void addCommentLike(CommentLike commentLike) {
        this.commentLikes.add(commentLike);
        commentLike.addComment(this);
    }

    public void addParentComment(Comment parent) {
        this.parent = parent;
    }

    public void addPost(Post post) { this.post = post; }

    public void deleteComment() {
        this.isDeleted = true;
    }

    public void deletedCommentLike(CommentLike commentLike) {
        this.commentLikes.remove(commentLike);
        commentLike.addComment(null);
    }

    public void incrementReportCount() {
        this.reportCount++;
    }
}
