package sw.study.user.service;

import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import sw.study.config.jwt.TokenDTO;
import sw.study.config.jwt.TokenProvider;
import sw.study.user.dto.LoginRequest;
import sw.study.user.util.RedisUtil;

import java.util.concurrent.TimeUnit;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;
    private final RedisUtil redisUtil;
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24;       // 1일
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 7;

    public AuthService(AuthenticationManager authenticationManager, TokenProvider tokenProvider, RedisUtil redisUtil) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.redisUtil = redisUtil;
    }

    public TokenDTO login(LoginRequest loginRequest) {
        String refreshTokenKey = "RT:" + loginRequest.getEmail();
        if (redisUtil.hasKey(refreshTokenKey)) { // 이메일을 키로 사용하여 Redis에 저장된 정보 확인
            // 이미 로그인된 경우, 해당 세션을 로그아웃 처리
            forceLogout(refreshTokenKey);
        }

        // 사용자 인증
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());
        authenticationManager.authenticate(authenticationToken);

        // 인증 성공 후 토큰 생성
        TokenDTO tokenDTO = tokenProvider.generateTokenDTO(authenticationToken);

        // 생성된 토큰 확인
        if (tokenDTO.getAccessToken() != null && tokenDTO.getRefreshToken() != null) {
            // Refresh Token을 Redis에 저장
            redisUtil.setData(refreshTokenKey, tokenDTO.getRefreshToken(), REFRESH_TOKEN_EXPIRE_TIME, TimeUnit.MILLISECONDS);

            // Access Token을 Redis에 저장 (예: "AT:이메일" 형식으로 저장)
            String accessTokenKey = "AT:" + loginRequest.getEmail();
            redisUtil.setData(accessTokenKey, tokenDTO.getAccessToken(), ACCESS_TOKEN_EXPIRE_TIME, TimeUnit.MILLISECONDS);

            return tokenDTO; // 생성된 토큰 반환
        } else {
            throw new RuntimeException("토큰 생성에 실패했습니다.");
        }
    }

    @Transactional
    public void logout(String refreshToken) {
        // Refresh Token 키를 생성
        Claims claims = tokenProvider.parseClaims(refreshToken);
        String email = claims.getSubject();
        String refreshTokenKey = "RT:" + email;

        // Refresh Token을 조회
        String storedRefreshToken = redisUtil.getData(refreshTokenKey);

        // Refresh Token이 존재하는 경우
        if (storedRefreshToken != null && storedRefreshToken.equals(refreshToken)) {
            // Access Token 키를 생성
            String accessTokenKey = "AT:" + email; // RT: 부분을 제거하고 "AT:" 접두사 추가
            String accessToken = redisUtil.getData(accessTokenKey); // Access Token 조회

            if (accessToken != null) {
                // Access Token의 유효성 검사
                if (tokenProvider.validateToken(accessToken)) {
                    Long expiration = tokenProvider.getExpiration(accessToken);
                    redisUtil.setBlackList("BlackList_" + accessToken, true, expiration, TimeUnit.MILLISECONDS);
                }

                // Redis에서 Access Token 삭제
                redisUtil.delete(accessTokenKey);
            }

            // Redis에서 Refresh Token 삭제
            redisUtil.delete(refreshTokenKey);
        }
    }

    @Transactional
    public void forceLogout(String refreshTokenKey) {
        // 해당 사용자의 Refresh Token을 조회
        String refreshToken = redisUtil.getData(refreshTokenKey);

        if (refreshToken != null) {
            // Access Token 키를 생성
            String accessTokenKey = "AT:" + refreshTokenKey.substring(3); // RT: 부분을 제거하고 "AT:" 접두사 추가
            String accessToken = redisUtil.getData(accessTokenKey); // Access Token 조회

            if (accessToken != null) {
                // Access Token의 유효성 검사
                if (tokenProvider.validateToken(accessToken)) {
                    Long expiration = tokenProvider.getExpiration(accessToken);
                    redisUtil.setBlackList("BlackList_" + accessToken, true, expiration, TimeUnit.MILLISECONDS);
                }

                // Redis에서 Access Token 삭제
                redisUtil.delete(accessTokenKey);
            }

            // Redis에서 Refresh Token 삭제
            redisUtil.delete(refreshTokenKey);
        }
    }

}
