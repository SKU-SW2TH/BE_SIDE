package sw.study.studyGroup.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
public class DailyLogRequest {

    private String title;
    private String content;
    private List<MultipartFile> fileUrls;

    public DailyLogRequest(String title, String content, List<MultipartFile> fileUrls) {
        this.title = title;
        this.content = content;
        this.fileUrls = fileUrls;
    }
}
