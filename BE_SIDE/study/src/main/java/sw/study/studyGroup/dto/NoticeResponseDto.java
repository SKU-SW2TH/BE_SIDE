package sw.study.studyGroup.dto;

import lombok.Data;
import sw.study.studyGroup.domain.Notice;

import java.time.LocalDateTime;

@Data
public class NoticeResponseDto {

    private Long id;
    private String nickname;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public NoticeResponseDto(Long id, String nickname, String title, String content, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.nickname = nickname;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // 목록 조회용 생성 메서드
    public static NoticeResponseDto fromList(Notice notice) {
        return new NoticeResponseDto(
                notice.getId(),
                notice.getTitle(),
                null,
                null,
                notice.getCreatedAt(),
                notice.getUpdatedAt()
        );
    }

    // 게시글 상세
    public static NoticeResponseDto fromDetail(Notice notice) {
        return new NoticeResponseDto(
                notice.getId(),
                notice.getTitle(),
                notice.getContent(),
                notice.getAuthor().getNickname(),
                notice.getCreatedAt(),
                notice.getUpdatedAt()
        );
    }
}
