package sw.study.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sw.study.admin.role.ReportTargetType;
import sw.study.user.domain.Member;
import sw.study.admin.role.ReportReason;
import sw.study.admin.role.ReportStatus;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "report")
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Report {

    @Id @GeneratedValue
    @Column(name = "report_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "reporter_id")
    private Member reporter;

    @ManyToOne
    @JoinColumn(name = "target_mem_id")
    private Member targetMember;

    private Long targetId;
    private String description;

    @Enumerated(EnumType.STRING)
    private ReportTargetType reportTargetType;
    @Enumerated(EnumType.STRING)
    private ReportReason reportReason;
    @Enumerated(EnumType.STRING)
    private ReportStatus reportStatus;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;



    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now(); // 생성 시 updatedAt 초기화
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public static Report createReport(Member reporter, Member targetMember, Long targetId, String description,
                                      ReportTargetType reportTargetType, ReportReason reportReason,
                                      ReportStatus reportStatus) {

        Report report = new Report();
        report.reporter = reporter;
        report.targetMember = targetMember;
        report.targetId = targetId;
        report.reportTargetType = reportTargetType;
        report.description = description;
        report.reportReason = reportReason;
        report.reportStatus = reportStatus;
        return report;
    }

}
