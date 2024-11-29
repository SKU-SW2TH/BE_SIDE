package sw.study.studyGroup.dto;

import lombok.Data;

@Data
public class DailyLogRequest {

    private String title;
    private String content;

    public DailyLogRequest() {}

    public DailyLogRequest(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
