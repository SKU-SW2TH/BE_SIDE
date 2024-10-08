package sw.study.user.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import sw.study.community.domain.InterestArea;
import java.time.LocalDateTime;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "mentorskill")
@Getter
@RequiredArgsConstructor(access = PROTECTED)
public class MentorSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mentor_skill_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // 다대일 관계 설정
    @JoinColumn(name = "mentor_id", nullable = false) // 외래키 이름 설정
    private Mentor mentor;

    @ManyToOne
    @JoinColumn(name = "interest_area_id", nullable = false)
    private InterestArea interestArea;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public static MentorSkill createMentorSkill(Mentor mentor, InterestArea interestArea) {
        MentorSkill mentorSkill = new MentorSkill();
        mentorSkill.mentor = mentor;
        mentorSkill.interestArea = interestArea;
        return mentorSkill;
    }

}
