package sw.study.user.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sw.study.exception.AccountDisabledException;
import sw.study.exception.DuplicateNicknameException;
import sw.study.exception.InvalidCredentialsException;
import sw.study.exception.MemberCreationException;
import sw.study.exception.UserNotFoundException;
import sw.study.exception.email.*;
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

        try {
            // 인증 코드 생성
            String verificationCode = memberService.createCode();

            // 인증 코드를 Redis에 저장 (만료 시간 10분)
            emailVerificationService.saveVerificationCode(email, verificationCode);

            // 이메일 발송
            String subject = "이메일 인증 코드";
            String text = "이메일 인증 코드는 다음과 같습니다: " + verificationCode;
            mailService.sendEmail(email, subject, text);
            return ResponseEntity.ok("인증 코드가 전송되었습니다.");


        } catch (VerificationCodeGenerationException e) {
            // 인증 코드 생성 중 오류발생
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage()); // 500
        } catch (VerificationCodeStorageException e) {
            // 인증 코드를 Redis에 저장 중 오류발생
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(e.getMessage()); // 503
        } catch (EmailSendException e) {
            // 이메일 전송 중 오류 발생
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage()); // 500
        } catch (Exception e) {
            // 기타 예외 발생
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage()); // 500
        }
    }

    @Override
    @PostMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestBody EmailVerificationRequest request) {

        try {
            // 인증 코드 검증
            emailVerificationService.verifyCode(request.getEmail(), request.getVerificationCode());
            // 이메일 중복 확인
            memberService.verifyEmail(request.getEmail());
            return ResponseEntity.ok("이메일 인증이 완료되었습니다."); // 이메일 인증 성공 시 처리 (프론트엔드에서 이를 확인)


        } catch (VerificationCodeMismatchException e) {
            // 인증번호가 일치하지 않음
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(e.getMessage()); // 422
        } catch (DuplicateEmailException e){
            // 이메일 중복
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage()); // 409
        } catch (Exception e) {
            // 기타 예외 발생
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @Override
    @PostMapping("/verify-nickname")
    public ResponseEntity<String> verifyNickname(@RequestBody NicknameDto nicknameDto) {

        try {
            memberService.verifyNickname(nicknameDto);
            return ResponseEntity.status(HttpStatus.OK).body("사용 가능한 닉네임입니다.");


        }catch (DuplicateNicknameException e){
            // 닉네임 중복 발생
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            // 기타 예외 발생
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @Override
    @PostMapping("/join")
    public ResponseEntity<String> join(@RequestBody JoinDto joinDto) {
        // 프론트엔드에서 이메일 인증 완료 여부를 확인 후 회원가입 진행
        try {
            // 이메일 중복 확인
            memberService.verifyEmail(joinDto.getEmail());
            // 회원가입
            memberService.join(joinDto);
            return ResponseEntity.ok("회원가입이 완료되었습니다.");


        } catch (DuplicateEmailException e){
            // 이메일 중복
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage()); // 409
        } catch (MemberCreationException e) {
            // 회원가입 중 발생한 예외
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage()); // 500
        } catch (Exception e) {
            // 기타 예외 발생
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage()); // 500
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
        } catch (AccountDisabledException e) {
            // 계정이 비활성화된 경우
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("계정이 비활성화되어 로그인할 수 없습니다."); // 403 Forbidden
        }catch (Exception e) {
            // 기타 예외 처리
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage()); // 500 Internal Server Error
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

    @Override
    @DeleteMapping("/delete-account")
    public ResponseEntity<String> deleteAccount(@RequestHeader("Authorization") String refreshToken) {
        try {
            String token = refreshToken.startsWith("Bearer ") ? refreshToken.substring(7) : refreshToken;
            authService.deleteMember(token);
            return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("사용자를 찾을 수 없습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("탈퇴 처리 중 오류가 발생했습니다.");
        }
    }

    @Override
    @PutMapping("/restore")
    public ResponseEntity<String> restoreMember(@RequestHeader("Authorization") String accessToken) {
        try {
            // 토큰 유효성 검사
            if (accessToken == null || !accessToken.startsWith("Bearer ")) {
                throw new IllegalArgumentException("[ERROR] 유효하지 않은 토큰 형식입니다.");
            }

            String token = accessToken.substring(7);

            // 회원 복구 처리
            authService.restoreMember(token);
            return ResponseEntity.ok("회원이 성공적으로 복구되었습니다.");
        } catch (IllegalArgumentException e) {
            // 토큰 형식 오류 또는 유효하지 않은 요청
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (UserNotFoundException e) {
            // 회원이 존재하지 않을 때
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }catch (Exception e) {
            // 그 외 예상치 못한 오류
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("회원 복구 중 오류가 발생했습니다.");
        }
    }
}