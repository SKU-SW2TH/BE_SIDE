package sw.study.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sw.study.config.jwt.TokenDTO;
import sw.study.config.jwt.TokenProvider;
import sw.study.user.dto.LoginRequest;
import sw.study.user.dto.LogoutRequest;
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
    public ResponseEntity<String> logout(@RequestBody LogoutRequest logoutRequest) {
        try {
            authService.logout(logoutRequest.getEmail(), logoutRequest.getRefreshToken()); // AuthService의 logout 메서드 호출
            return ResponseEntity.ok("로그아웃 성공"); // 200 OK 응답
        } catch (Exception ex) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR) // 500 Internal Server Error
                    .build(); // 본문은 null
        }
    }

    @PostMapping("/reissue")
    public ResponseEntity<TokenDTO> reissue(@RequestBody TokenRequest tokenRequest) {
        TokenDTO newToken = tokenProvider.reissueAccessToken(tokenRequest.getRefreshToken());
        return ResponseEntity.ok(newToken);
    }

}

