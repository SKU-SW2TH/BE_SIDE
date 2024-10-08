package sw.study.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.misc.NotNull;
import sw.study.admin.role.Reason;
import sw.study.admin.role.Status;
import sw.study.admin.role.TargetType;
import sw.study.community.domain.Member;
import java.time.LocalDateTime;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "report")
@Getter
@RequiredArgsConstructor(access = PROTECTED)
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // 다대일 관계 설정
    @JoinColumn(name = "member_id") // 외래키 이름 설정
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY) // 다대일 관계 설정
    @JoinColumn(name = "target_mem_id") // 외래키 이름 설정
    private Member targetMember;

    private long targetId;

    @NotNull
    @Enumerated(EnumType.STRING) // Enum 타입을 문자열로 저장
    private TargetType targetType;

    @Lob
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING) // Enum 타입을 문자열로 저장
    private Reason reason;

    @Enumerated(EnumType.STRING) // Enum 타입을 문자열로 저장
    private Status status;

    private LocalDateTime createdAt;

    private LocalDateTime reportedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.reportedAt = LocalDateTime.now(); // 생성 시 reportedAt 초기화
    }

    @PreUpdate
    protected void onUpdate() {
        this.reportedAt = LocalDateTime.now();
    }

    public static Report createReport(Member member, Member targetMember, long targetId, String description, TargetType targetType, Reason reason, Status status) {
        Report report = new Report();
        report.member = member;
        report.targetMember = targetMember;
        report.targetId = targetId;
        report.targetType = targetType;
        report.description = description;
        report.reason = reason;
        report.status = status;
        return report;
    }

}
