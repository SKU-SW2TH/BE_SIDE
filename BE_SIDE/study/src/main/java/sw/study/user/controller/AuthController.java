package sw.study.user.controller;

import jakarta.security.auth.message.AuthException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sw.study.config.jwt.TokenDTO;
import sw.study.user.dto.LoginRequest;
import sw.study.user.service.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
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

}

