package sw.study.studyGroup.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NoticeRequestDto {
    private String title;
    private String content;

    public NoticeRequestDto(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
