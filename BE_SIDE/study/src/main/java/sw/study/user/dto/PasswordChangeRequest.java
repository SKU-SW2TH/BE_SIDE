package sw.study.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PasswordChangeRequest {
    @Schema(description = "현재 비밀번호")
    String oldPassword;

    @Schema(description = "새 비밀번호")
    String newPassword;
}
