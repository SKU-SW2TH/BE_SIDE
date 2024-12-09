package sw.study.user.service;

import io.jsonwebtoken.Claims;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import sw.study.config.jwt.JWTService;
import sw.study.config.jwt.TokenDTO;
import sw.study.config.jwt.TokenProvider;
import sw.study.exception.*;
import sw.study.user.domain.Member;
import sw.study.user.dto.LoginRequest;
import sw.study.user.repository.MemberRepository;
import sw.study.user.util.RedisUtil;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;
    private final RedisUtil redisUtil;
    private final MemberRepository memberRepository;
    private final JWTService jwtService;
    private final BCryptPasswordEncoder encoder;
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24;       // 1일
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 7;
    private static final long PASSWORD_RESET_TOKEN_EXPIRE_TIME = 1000 * 60 * 10;// 10분

    public TokenDTO login(LoginRequest loginRequest) {
        String refreshTokenKey = "RT:" + loginRequest.getEmail();

        if (redisUtil.hasKey(refreshTokenKey)) { // Redis에서 이메일 키 확인
            // 이미 로그인된 경우, 해당 세션을 로그아웃 처리
            forceLogout(refreshTokenKey);
        }

        // 사용자 인증
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());

        try {
            Authentication authentication = authenticationManager.authenticate(authenticationToken);

            // 인증 성공 후 토큰 생성
            TokenDTO tokenDTO = tokenProvider.generateTokenDTO(authentication);

            // 생성된 토큰 확인
            if (tokenDTO.getAccessToken() != null && tokenDTO.getRefreshToken() != null) {
                // Refresh Token을 Redis에 저장
                redisUtil.setData(refreshTokenKey, tokenDTO.getRefreshToken(), REFRESH_TOKEN_EXPIRE_TIME, TimeUnit.MILLISECONDS);

                // Access Token을 Redis에 저장
                String accessTokenKey = "AT:" + loginRequest.getEmail();
                redisUtil.setData(accessTokenKey, tokenDTO.getAccessToken(), ACCESS_TOKEN_EXPIRE_TIME, TimeUnit.MILLISECONDS);

                return tokenDTO; // 생성된 토큰 반환
            } else {
                throw new RuntimeException("토큰 생성에 실패했습니다.");
            }
        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException("유효하지 않은 이메일 또는 패스워드입니다."); // 사용자 정의 예외 또는 적절한 예외로 처리
        } catch (Exception e) {
            throw new RuntimeException("로그인에 실패했습니다."); // 기타 예외 처리
        }
    }

    @Transactional
    public void logout(String refreshToken) {
        // Refresh Token 키를 생성
        String email = jwtService.extractEmail(refreshToken);
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

    @Transactional
    public void deleteMember(String refreshToken) {
        String token = jwtService.extractToken(refreshToken);
        String email = jwtService.extractEmail(token);
        String refreshTokenKey = "RT:" + email;

        // 회원 삭제 처리
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        member.requestDeactivation(); // soft delete 방식으로 설정

        memberRepository.save(member);
        // 강제 로그아웃 처리
        forceLogout(refreshTokenKey);
    }

    @Transactional
    public void restoreMember(String accessToken) {
        String token = jwtService.extractToken(accessToken);
        String email = jwtService.extractEmail(token);

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다."));

        // 복구 처리
        member.restore();
        memberRepository.save(member);
    }

    // 비밀번호 변경시키는 메서드
    @Transactional
    public void changePassword(String token, String newPassword) {
        token = jwtService.extractToken(token);
        String email = jwtService.extractEmail(token);
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        // 비밀번호 공백 제거 및 암호화
        String trimmedNewPassword = newPassword.trim();

        // 기존 비밀번호와 새 비밀번호가 같은지 확인
        if (encoder.matches(trimmedNewPassword, member.getPassword())) throw new SamePasswordException("변경하려는 비밀번호가 기존 비밀번호와 같습니다.");

        String encodedNewPassword = encoder.encode(trimmedNewPassword);
        member.changePassword(encodedNewPassword);
        memberRepository.save(member);

        // redis에서 해당 토큰 삭제
        redisUtil.delete("PT:" + email);
    }

    // 이메일을 입력받아서 jwt토큰을 생성하고, 이를 Redis에 저장, 사용자에게 반환
    public String generatePasswordResetToken(String email) {
        email = email.replace("\"", ""); // 큰 따옴표 제거
        String token = tokenProvider.generatePasswordResetToken(email);
        redisUtil.setData("PT:" + email, token, PASSWORD_RESET_TOKEN_EXPIRE_TIME, TimeUnit.MILLISECONDS);
        return token;
    }

    // 비밀번호 변경 토큰의 유효성 검사를 하는 메서드
    public void validPasswordResetToken(String token) {
        token = jwtService.extractToken(token);
        String email = jwtService.extractEmail(token);
        if(!redisUtil.getData("PT:" + email).equals(token)) {
            throw new InvalidTokenException("유효성 검사를 통과하지 못했습니다.");
        }
    }

}
