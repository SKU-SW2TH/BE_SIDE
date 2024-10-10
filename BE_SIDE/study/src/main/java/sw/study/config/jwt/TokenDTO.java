package sw.study.config.jwt;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenDTO {

    private String grantType;              // 토큰 유형 (일반적으로 "Bearer"를 사용)
    private String accessToken;            // 액세스 토큰 (JWT)
    private String refreshToken;           // 리프레시 토큰 (JWT)
    private Long accessTokenExpiresIn;     // 액세스 토큰의 만료 시간 (Unix timestamp 또는 기간)
}
