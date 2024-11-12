package sw.study.community.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sw.study.user.domain.Area;
import sw.study.user.domain.Member;

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
    private Category category;  // 1: 자유게시판, 2: 질문게시판

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private List<PostFile> files = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostLike> likes = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostArea> interests = new ArrayList<>();

    private String title;
    private String content;
    private boolean isDeleted = false;
    private int viewCount = 0;
    private int reportCount = 0;
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
    public static Post createPost(String title, String content, Category category,
                                  Member member, List<Area> areas,
                                  List<String> urls) {
        Post post = new Post();
        post.title = title;
        post.content = content;

        post.member = member;
        post.category = category;

        for (Area area : areas) {
            PostArea postArea = PostArea.createPostArea(area);
            post.addInterest(postArea);
        }

        for (String url : urls) {
            PostFile postFile = PostFile.createPostFile(member, url);
            post.addFile(postFile);
        }

        return post;
    }

    //== 연관 관계 편의 메서드 ==//
    public void addComment(Comment comment) {
        this.comments.add(comment);
        comment.addPost(this);
    }

    public void addFile(PostFile file) {
        this.files.add(file);
        file.addPost(this);
    }

    public void addInterest(PostArea interest) {
        this.interests.add(interest);
        interest.addPost(this);
    }

    public void addLike(PostLike like) {
        this.likes.add(like);
        like.addPost(this);
    }

    //== 비지니스 로직 ==//
    public void deletePost() {
        this.isDeleted = true;
    }

    public void removeLike(PostLike postLike) {
        this.likes.remove(postLike);
        postLike.addPost(null); // Post를 null로 설정하여 양방향 관계를 유지
    }

    public void incrementReportCount() {
        this.reportCount++;
    }
}
