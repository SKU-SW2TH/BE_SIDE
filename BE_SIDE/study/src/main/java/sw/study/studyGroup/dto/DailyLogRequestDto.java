package sw.study.studyGroup.dto;

import lombok.Data;
import sw.study.studyGroup.domain.DailyLog;

@Data
public class DailyLogRequestDto {

    private String title;
    private String content;

    public DailyLogRequestDto(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
