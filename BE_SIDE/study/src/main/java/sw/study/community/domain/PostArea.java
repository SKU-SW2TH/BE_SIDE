package sw.study.community.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sw.study.user.domain.Area;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class PostArea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_interest_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne
    @JoinColumn(name = "interest_area_id")
    private Area area;

    //== 생성 메서드 ==//
    public static PostArea createPostArea(Area area) {
        PostArea postArea = new PostArea();
        postArea.area = area;

        return postArea;
    }

    public void addPost(Post post) {
        this.post = post;
    }
}
