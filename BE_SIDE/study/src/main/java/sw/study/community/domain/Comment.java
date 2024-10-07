package sw.study.community.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne
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
        comment.post = post;
        comment.member = member;
        comment.content = content;
        comment.level = level;

        return comment;
    }

    public void addChildComment(Comment child) {
        this.child.add(child);
        child.addParentComment(this);
    }

    public void addParentComment(Comment parent) {
        this.parent = parent;
    }
}
