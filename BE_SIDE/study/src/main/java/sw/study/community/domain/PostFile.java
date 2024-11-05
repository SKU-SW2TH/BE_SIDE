package sw.study.community.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sw.study.user.domain.Member;

import java.time.LocalDateTime;

import static lombok.AccessLevel.*;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class PostFile {
    @Id @GeneratedValue
    @Column(name = "file_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    private String url;
    private boolean isDeleted = false;
    private LocalDateTime createdAt;


    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    //== 생성 메서드 ==//
    public static PostFile createPostFile(Member member, String url) {
        PostFile file = new PostFile();
        file.member = member;
        file.url = url;

        return file;
    }


    public void addPost(Post post) {
        this.post = post;
    }
}
