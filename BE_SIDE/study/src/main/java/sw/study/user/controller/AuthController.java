package sw.study.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sw.study.user.dto.EmailDto;
import sw.study.user.dto.EmailVerificationRequest;
import sw.study.user.dto.MemberDto;
import sw.study.user.service.EmailVerificationService;
import sw.study.user.service.MailService;
import sw.study.user.service.MemberService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final EmailVerificationService emailVerificationService;
    private final MailService mailService;
    private final MemberService memberService;

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

    @PostMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestBody EmailVerificationRequest request) {
        // 인증 코드 검증
        boolean isVerified = emailVerificationService.verifyCode(request.getEmail(), request.getVerificationCode());

        if (isVerified)
            return ResponseEntity.ok("이메일 인증이 완료되었습니다."); // 이메일 인증 성공 시 처리 (프론트엔드에서 이를 확인)
        else
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("인증 코드가 올바르지 않습니다.");
    }

    @PostMapping("/verify-nickname")
    public ResponseEntity<String> verifyNickname(@RequestBody MemberDto memberDto) {
        boolean isVerified = memberService.verifyNickname(memberDto);

        if (isVerified)
            return ResponseEntity.ok("사용 가능한 닉네임입니다.");
        else
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("이미 사용 중인 닉네임입니다.");
    }

    @PostMapping("/join")
    public ResponseEntity<String> join(@RequestBody MemberDto memberDto) {
        // 프론트엔드에서 이메일 인증 완료 여부를 확인 후 회원가입 진행

        // 이메일 중복 확인
        boolean isVerified = memberService.verifyEmail(memberDto);

        if (isVerified) {
            memberService.join(memberDto);
            return ResponseEntity.ok("회원가입이 완료되었습니다.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("이미 사용 중인 이메일입니다.");
        }
    }
}