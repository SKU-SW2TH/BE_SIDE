package sw.study.user.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UpdateProfileRequest {
    private String nickname;
    private MultipartFile profilePicture; // MultipartFile로 변경
    private String introduction;
}
