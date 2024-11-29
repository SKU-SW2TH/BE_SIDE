package sw.study.studyGroup.dto;

import lombok.Data;
import sw.study.studyGroup.domain.Notice;

import java.time.LocalDateTime;

@Data
public class NoticeResponse {

    private Long id;
    private String nickname;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public NoticeResponse(Long id, String nickname, String title, String content, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.nickname = nickname;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // 목록 조회용 생성 메서드
    public static NoticeResponse fromList(Notice notice) {
        return new NoticeResponse(
                notice.getId(),
                notice.getAuthor().getNickname(),
                notice.getTitle(),
                null, // 목록 조회이기에 본문은 제외
                notice.getCreatedAt(),
                notice.getUpdatedAt()
        );
    }

    // 게시글 상세
    public static NoticeResponse fromDetail(Notice notice) {
        return new NoticeResponse(
                notice.getId(),
                notice.getAuthor().getNickname(),
                notice.getTitle(),
                notice.getContent(),
                notice.getCreatedAt(),
                notice.getUpdatedAt()
        );
    }
}
