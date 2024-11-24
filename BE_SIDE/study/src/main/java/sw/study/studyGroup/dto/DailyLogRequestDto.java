package sw.study.studyGroup.dto;

import lombok.Data;

@Data
public class DailyLogRequestDto {

    private String title;
    private String content;

    public DailyLogRequestDto() {}

    public DailyLogRequestDto(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
