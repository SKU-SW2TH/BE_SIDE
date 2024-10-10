package sw.study.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import sw.study.user.domain.Member;

import java.time.LocalDateTime;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "punishment")
@Getter
@RequiredArgsConstructor(access = PROTECTED)
public class Punishment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "punishment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // 다대일 관계 설정
    @JoinColumn(name = "member_id") // 외래키 이름 설정
    private Member member;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    // 생성자 또는 정적 팩토리 메서드
    public static Punishment createPunishment(Member member, int durationInDays) {
        Punishment punishment = new Punishment();
        punishment.member = member;
        punishment.startDate = LocalDateTime.now(); // 현재 시간으로 시작일 설정
        punishment.endDate = punishment.startDate.plusDays(durationInDays); // 기간을 더하여 종료일 설정
        return punishment;
    }

}
