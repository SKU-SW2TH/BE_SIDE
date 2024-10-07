package sw.study.community.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
        postLike.post = post;
        postLike.member = member;
        return postLike;
    }

    // 좋아요 눌렀을때 추가되는 로직을 여기서 하는게 좋을까 서비스 계층에서 만드는게 좋을까?
}


