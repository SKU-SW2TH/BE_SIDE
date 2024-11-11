package sw.study.studyGroup.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sw.study.user.domain.Member;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WaitingPeople {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "waiting_id")
    private Long id; // 대기 ID (PK)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id") // 그룹 ID (FK)
    private StudyGroup studyGroup; // 스터디 그룹 참조

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id") // 멤버 ID (FK)
    private Member member; // 멤버 참조

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt; // 생성 날짜

    // 정적 팩토리 메소드
    public static WaitingPeople createWaitingPerson(Member member, StudyGroup studyGroup) {
        WaitingPeople waitingPerson = new WaitingPeople();
        waitingPerson.member = member;
        waitingPerson.studyGroup = studyGroup;
        waitingPerson.createdAt = LocalDateTime.now();
        return waitingPerson;
    }
}
