package sw.study.mentoring.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "review")
@Getter
@RequiredArgsConstructor(access = PROTECTED)
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // 다대일 관계 설정
    @JoinColumn(name = "mentor_id", nullable = false) // 외래키 이름 설정
    private Mentor mentor;

    private double rating;

    @Lob
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    private boolean is_deleted = false;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void onDelete() {
        this.is_deleted = true;
    }

    public static Review createReview(Mentor mentor, String description, double rating){
        Review review = new Review();
        review.mentor = mentor;
        review.rating = rating;
        review.description = description;
        return review;
    }

}
