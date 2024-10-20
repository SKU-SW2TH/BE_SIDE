package sw.study.user.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sw.study.exception.InvalidCredentialsException;
import sw.study.exception.UserNotFoundException;
import sw.study.user.apiDoc.AuthApiDocumentation;
import sw.study.user.dto.*;
import sw.study.user.service.EmailVerificationService;
import sw.study.user.service.MailService;
import sw.study.user.service.MemberService;
import sw.study.config.jwt.TokenDTO;
import sw.study.config.jwt.TokenProvider;
import sw.study.user.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Auth API")
public class AuthController implements AuthApiDocumentation {
    private final AuthService authService;
    private final TokenProvider tokenProvider;

    private final EmailVerificationService emailVerificationService;
    private final MailService mailService;
    private final MemberService memberService;

    @Override
    @PostMapping("/send-verification-email")
    public ResponseEntity<String> sendVerificationEmail(@RequestBody EmailDto emailDto) {
        String email = emailDto.getEmail();

        // 인증 코드 생성
        String verificationCode = memberService.createCode();


        // 인증 코드를 Redis에 저장 (만료 시간 10분)
        emailVerificationService.saveVerificationCode(email, verificationCode);


        // 이메일 발송
        String subject = "이메일 인증 코드";
        String text = "이메일 인증 코드는 다음과 같습니다: " + verificationCode;
        mailService.sendEmail(email, subject, text);

        return ResponseEntity.ok("인증 코드가 전송되었습니다.");
    }

    @Override
    @PostMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestBody EmailVerificationRequest request) {
        // 인증 코드 검증
        boolean isVerified = emailVerificationService.verifyCode(request.getEmail(), request.getVerificationCode());

        if (isVerified)
            return ResponseEntity.ok("이메일 인증이 완료되었습니다."); // 이메일 인증 성공 시 처리 (프론트엔드에서 이를 확인)
        else
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("인증 코드가 올바르지 않습니다.");
    }

    @Override
    @PostMapping("/verify-nickname")
    public ResponseEntity<String> verifyNickname(@RequestBody JoinDto joinDto) {
        boolean isVerified = memberService.verifyNickname(joinDto);

        if (isVerified)
            return ResponseEntity.ok("사용 가능한 닉네임입니다.");
        else
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("이미 사용 중인 닉네임입니다.");
    }

    @Override
    @PostMapping("/join")
    public ResponseEntity<String> join(@RequestBody JoinDto joinDto) {
        // 프론트엔드에서 이메일 인증 완료 여부를 확인 후 회원가입 진행

        // 이메일 중복 확인
        boolean isVerified = memberService.verifyEmail(joinDto);

        if (isVerified) {
            memberService.join(joinDto);
            return ResponseEntity.ok("회원가입이 완료되었습니다.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("이미 사용 중인 이메일입니다.");
        }
    }

    @Override
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            TokenDTO tokenDTO = authService.login(loginRequest); // AuthService의 login 메서드 호출
            return ResponseEntity.ok(tokenDTO); // 200 OK와 함께 토큰 반환
        } catch (InvalidCredentialsException e) {
            // 자격 증명이 잘못된 경우
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(e.getMessage()); // 401 Unauthorized
        } catch (UserNotFoundException e) {
            // 사용자가 존재하지 않는 경우
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage()); // 404 Not Found
        } catch (Exception e) {
            // 기타 예외 처리
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while processing your request."); // 500 Internal Server Error
        }
    }

    @Override
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

    @Override
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