package sw.study.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenRequest {
    @Schema(description = "리프레시 토큰", example = "your_refresh_token")
    private String refreshToken;
}
