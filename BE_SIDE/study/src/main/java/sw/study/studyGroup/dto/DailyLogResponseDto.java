package sw.study.studyGroup.dto;

import lombok.Data;
import sw.study.studyGroup.domain.DailyLog;

import java.time.LocalDateTime;

@Data
public class DailyLogResponseDto {

    private Long logId;
    private String title;
    private String content;
    private String authorName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public DailyLogResponseDto(DailyLog dailyLog) {
        this.logId = dailyLog.getId();
        this.title = dailyLog.getTitle();
        this.content = dailyLog.getContent();
        this.authorName = dailyLog.getAuthor().getNickname();
        this.createdAt = dailyLog.getCreatedAt();
        this.updatedAt = dailyLog.getUpdatedAt();
    }
}
