package sw.study.studyGroup.dto;

import lombok.Data;
import sw.study.studyGroup.domain.Notice;

import java.time.LocalDateTime;

@Data
public class NoticeListResponse {
    private Long id;
    private String nickname;
    private String title;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public NoticeListResponse(Long id, String nickname, String title, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.nickname = nickname;
        this.title = title;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static NoticeListResponse createNoticeList(Notice notice) {
        return new NoticeListResponse(
                notice.getId(),
                notice.getAuthor().getNickname(),
                notice.getTitle(),
                notice.getCreatedAt(),
                notice.getUpdatedAt()
        );
    }
}
