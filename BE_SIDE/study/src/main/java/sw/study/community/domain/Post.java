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
public class Post {

    @Id @GeneratedValue
    @Column(name = "post_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "post") // 게시판 쪽에 있어야 불러오기 편한거 아닌가?
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "post") // 게시판 쪽에 있어야 불러오기 편한거 아닌가?
    private List<File> files = new ArrayList<>();

    @OneToMany(mappedBy = "post") // 게시판 쪽에 있어야 불러오기 편한거 아닌가?
    private List<PostLike> likes = new ArrayList<>();

    private String title;
    private String content;
    private boolean isDeleted = false;
    private int viewCount = 0;
    private int reportCount = 0;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 삭제 여부를 변경하는 것도 수정에 포함되는데 이게 꼭 필요할까?
    // 삭제 여부가 true인 게시글의 updateAt가 곧 삭제 날짜 아닌가?


    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }


    //== 생성 메서드 ==//
    public static Post createPost(String title, String content, Category category, Member member) {
        Post post = new Post();
        post.title = title;
        post.content = content;

        post.member = member;
        post.category = category;

        return post;
    }

    //== 비지니스 로직 ==//

}
