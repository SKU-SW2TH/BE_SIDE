package sw.study.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import sw.study.exception.email.VerificationCodeMismatchException;
import sw.study.exception.email.VerificationCodeStorageException;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {

    private final RedisTemplate<String, Object> redisTemplate;

    // 인증 코드 저장
    public void saveVerificationCode(String email, String verificationCode) {
        try {
            redisTemplate.opsForValue().set(email, verificationCode);
            redisTemplate.expire(email, 10, TimeUnit.MINUTES);  // 10분 만료 시간
        } catch (Exception e) {
            log.error("Error storing verification code in Redis for email: {}", email, e);
            throw new VerificationCodeStorageException("인증 코드를 저장하는 중 오류가 발생했습니다.", e);
        }
    }

    // 인증 코드 검증
    public void verifyCode(String email, String verificationCode) {
        String storedCode = (String) redisTemplate.opsForValue().get(email);
        if (!verificationCode.equals(storedCode)) throw new VerificationCodeMismatchException("인증 코드가 일치하지 않습니다.");
    }
}
