package sw.study.studyGroup.dto;

import lombok.Data;
import sw.study.studyGroup.domain.Notice;

import java.time.LocalDateTime;

@Data
public class NoticeDetailResponse {

    private Long id;
    private String nickname;
    private String title;
    private String content;
    private boolean isChecked;
    private int numOfChecks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public NoticeDetailResponse(
            Long id, String nickname, String title, String content,
            LocalDateTime createdAt, LocalDateTime updatedAt,
            boolean isChecked, int numOfChecks) {
        this.id = id;
        this.nickname = nickname;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isChecked = isChecked;
        this.numOfChecks = numOfChecks;
    }

    public static NoticeDetailResponse createNoticeDetail(Notice notice, boolean isChecked, int numOfChecks) {
        return new NoticeDetailResponse(
                notice.getId(),
                notice.getAuthor().getNickname(),
                notice.getTitle(),
                notice.getContent(),
                notice.getCreatedAt(),
                notice.getUpdatedAt(),
                isChecked,
                numOfChecks
        );
    }
}
