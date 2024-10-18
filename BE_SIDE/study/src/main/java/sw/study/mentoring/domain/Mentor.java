package sw.study.mentoring.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import sw.study.mentoring.role.TimeAvailability;
import sw.study.user.domain.Member;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "mentor")
@Getter
@RequiredArgsConstructor(access = PROTECTED)
public class Mentor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mentor_id")
    private Long id;

    @OneToOne // 일대일 관계 설정
    @JoinColumn(name = "member_id") // 외래키 이름 설정
    private Member member;

    private double rating;

    @Enumerated(EnumType.STRING) // Enum 타입을 DB에 문자열로 저장
    private TimeAvailability time;

    private boolean is_deleted = false;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "mentor", cascade = CascadeType.ALL, orphanRemoval = true) // cascade 및 orphanRemoval 설정
    private List<MentorSkill> skills = new ArrayList<>();

    @OneToMany(mappedBy = "mentor", cascade = CascadeType.ALL, orphanRemoval = true) // cascade 및 orphanRemoval 설정
    private List<Review> reviews = new ArrayList<>();

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

    public static Mentor createMentor(Member member, String expertise, double rating, TimeAvailability time) {
        Mentor mentor = new Mentor();
        mentor.member = member;
        mentor.rating = rating;
        mentor.time = time;
        return mentor;
    }

    public void addSkill(MentorSkill skill) {
        skills.add(skill);
    }

    public void addReview(Review review) {
        reviews.add(review);
    }

}
