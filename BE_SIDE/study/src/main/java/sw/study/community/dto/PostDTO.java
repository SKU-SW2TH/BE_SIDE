package sw.study.community.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class PostDTO {
    private Long memberId;
    private String title;
    private String content;
    private String category;
    private List<String> area;
    private List<MultipartFile> files;
}
