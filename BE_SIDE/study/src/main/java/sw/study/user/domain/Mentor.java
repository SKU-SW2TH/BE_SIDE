package sw.study.user.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import sw.study.community.domain.Member;
import sw.study.user.TimeAvailability;

import java.time.LocalDateTime;

@Entity
@Table(name = "mentor")
@Getter
@RequiredArgsConstructor
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

}
