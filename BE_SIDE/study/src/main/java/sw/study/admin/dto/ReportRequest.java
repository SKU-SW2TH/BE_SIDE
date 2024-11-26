package sw.study.admin.dto;

import lombok.Data;
import sw.study.admin.role.ReportReason;
import sw.study.admin.role.ReportTargetType;

@Data
public class ReportRequest {
    private Long reporterId; // 신고자 ID
    private String description; // 설명
    private ReportReason reportReason; // 신고 사유
    private ReportTargetType reportTargetType; // 신고 대상 (게시글/댓글)
}
