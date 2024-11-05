package sw.study.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @Schema(description = "사용자의 이메일", example = "ksh123@naver.com")
    private String email;

    @Schema(description = "사용자의 비밀번호")
    private String password;
}

