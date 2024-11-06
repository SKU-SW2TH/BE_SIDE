package sw.study.community.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sw.study.user.domain.InterestArea;
import sw.study.user.domain.Member;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class PostInterest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_interest_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interest_area_id")
    private InterestArea interestArea;

    //== 생성 메서드 ==//
    public static PostInterest createPostInterest(InterestArea interestArea) {
        PostInterest postInterest = new PostInterest();
        postInterest.interestArea = interestArea;

        return postInterest;
    }

    public void addPost(Post post) {
        this.post = post;
    }
}
