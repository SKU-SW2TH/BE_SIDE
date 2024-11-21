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
public class PostLike {

    @Id @GeneratedValue
    @Column(name = "post_like_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    //== 생성 메서드 ==//
    public static PostLike createPostLike(Post post, Member member) {
        PostLike postLike = new PostLike();
        post.addLike(postLike);
        postLike.member = member;
        return postLike;
    }

    public void addPost(Post post) {
        this.post = post;
    }
}


