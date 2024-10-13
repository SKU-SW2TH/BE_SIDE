package sw.study.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sw.study.config.jwt.TokenDTO;
import sw.study.config.jwt.TokenProvider;
import sw.study.user.dto.LoginRequest;
import sw.study.user.dto.TokenRequest;
import sw.study.user.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final TokenProvider tokenProvider;

    public AuthController(AuthService authService, TokenProvider tokenProvider) {
        this.authService = authService;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenDTO> login(@RequestBody LoginRequest loginRequest) {
        try {
            TokenDTO tokenDTO = authService.login(loginRequest); // AuthService의 login 메서드 호출
            return ResponseEntity.ok(tokenDTO); // 200 OK와 함께 토큰 반환
        } catch (Exception ex) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR) // 500 Internal Server Error
                    .body(null); // 본문은 null
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody TokenRequest logoutRequest) {
        try {
            authService.logout(logoutRequest.getRefreshToken()); // AuthService의 logout 메서드 호출
            return ResponseEntity.ok("로그아웃 성공"); // 200 OK 응답
        } catch (Exception ex) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR) // 500 Internal Server Error
                    .build(); // 본문은 null
        }
    }

    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(@RequestBody TokenRequest tokenRequest) {
        try {
            // 토큰 재발행 로직 수행
            TokenDTO newToken = tokenProvider.reissueAccessToken(tokenRequest.getRefreshToken());
            return ResponseEntity.ok(newToken);
        } catch (RuntimeException e) {
            // 토큰 재발행 실패 시 발생한 예외 처리
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("토큰 재발행에 실패했습니다: " + e.getMessage());
        } catch (Exception e) {
            // 기타 예외 처리
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 에러가 발생했습니다.");
        }
    }

}

