package sw.study.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final RedisTemplate<String, Object> redisTemplate;

    // 인증 코드 저장
    public void saveVerificationCode(String email, String verificationCode) {
        redisTemplate.opsForValue().set(email, verificationCode);
        redisTemplate.expire(email, 10, TimeUnit.MINUTES);  // 10분 만료 시간
    }

    // 인증 코드 검증
    public boolean verifyCode(String email, String verificationCode) {
        String storedCode = (String) redisTemplate.opsForValue().get(email);
        return verificationCode.equals(storedCode);
    }
}
