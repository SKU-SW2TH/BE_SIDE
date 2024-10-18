package sw.study.user.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UpdateProfileResponse {
    private String nickname;
    private String profileURL; // MultipartFile로 변경
    private String introduction;

    public UpdateProfileResponse(String nickname, String introduction, String profileURL) {
        this.nickname = nickname;
        this.introduction = introduction;
        this.profileURL = profileURL;
    }

}
