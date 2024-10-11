package sw.study.user.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import sw.study.config.jwt.TokenDTO;
import sw.study.config.jwt.TokenProvider;
import sw.study.user.dto.LoginRequest;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;

    public AuthService(AuthenticationManager authenticationManager, TokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    public TokenDTO login(LoginRequest loginRequest) {
        // 사용자 인증
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());
        authenticationManager.authenticate(authenticationToken);

        // 인증 성공 후 토큰 생성
        TokenDTO tokenDTO = tokenProvider.generateTokenDTO(authenticationToken);

        // 생성된 토큰 확인
        if (tokenDTO.getAccessToken() != null && tokenDTO.getRefreshToken() != null) {
            return tokenDTO; // 생성된 토큰 반환
        } else {
            throw new RuntimeException("토큰 생성에 실패했습니다.");
        }
    }
}
