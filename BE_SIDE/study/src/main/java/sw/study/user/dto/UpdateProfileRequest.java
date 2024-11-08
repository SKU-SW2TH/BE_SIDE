package sw.study.user.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UpdateProfileRequest {
    private String nickname;
    private String introduction;

    public UpdateProfileRequest(String nickname, String introduction) {
        this.nickname = nickname;
        this.introduction = introduction;
    }

}
