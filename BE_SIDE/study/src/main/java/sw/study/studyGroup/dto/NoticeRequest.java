package sw.study.studyGroup.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NoticeRequest {
    private String title;
    private String content;

    public NoticeRequest(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
