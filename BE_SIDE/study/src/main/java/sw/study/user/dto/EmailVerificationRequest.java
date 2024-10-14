package sw.study.user.dto;

import lombok.Data;

// 이메일 인증을 위한 정보를 받는 객체
@Data
public class EmailVerificationRequest {
    private String email;
    private String verificationCode;

}
