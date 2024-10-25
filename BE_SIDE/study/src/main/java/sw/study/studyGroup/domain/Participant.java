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
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "participant_id") // 참가자 ID
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member; // FK (회원 테이블 참조)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id") // JoinColumn : FK -> 참조할 테이블 설정
    private StudyGroup studyGroup;  // FK (스터디그룹 테이블 참조)

    @Column(nullable = false, unique = true)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // 역할

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt; // 가입일

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 수정일(?)

    public enum Role {
        LEADER, MANAGER, MEMBER, MENTOR
    }

    public static Participant createParticipant(String nickname, Member member, Role role) {
        Participant participant = new Participant();
        participant.member = member;
        participant.nickname = nickname;
        participant.role = role;
        participant.joinedAt = LocalDateTime.now();
        participant.updatedAt = LocalDateTime.now();
        return participant;
    }
}
