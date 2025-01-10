package sw.study.studyGroup.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import sw.study.studyGroup.domain.DailyLog;
import sw.study.studyGroup.domain.DailyLogFile;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class DailyLogResponse {

    private Long logId;
    private String title;
    private String content;
    private String authorName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> fileUrls;

    public static DailyLogResponse createDailyLogResponse(DailyLog dailyLog){

        // 존재하면, 각각의 url 을 list 에 추가하고 없으면 빈 리스트로 처리
        List<String> fileUrls = (dailyLog.getFileUrls() != null ?
                dailyLog.getFileUrls().stream()
                        .map(DailyLogFile::getFileUrl)
                        .collect(Collectors.toList())
                : Collections.emptyList());

        // 생성자 호출
        return new DailyLogResponse(
                dailyLog.getId(),
                dailyLog.getTitle(),
                dailyLog.getContent(),
                dailyLog.getAuthor().getNickname(),
                dailyLog.getCreatedAt(),
                dailyLog.getUpdatedAt(),
                fileUrls
        );
    }
}
