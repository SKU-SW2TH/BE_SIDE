package sw.study.config.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class JWTService {
    private final TokenProvider tokenProvider;

    public String extractEmail(String token) {
        try {
            Claims claims = tokenProvider.parseClaims(token);
            return claims.getSubject(); // 이메일이 subject에 저장된 경우
        } catch (ExpiredJwtException e) {
            log.warn("Token has expired: {}", token);
            throw new IllegalStateException("만료된 토큰");
        } catch (JwtException e) {
            log.warn("Invalid JWT token: {}", token);
            throw new IllegalArgumentException("존재하지 않는 토큰 형식");
        }
    }

}
