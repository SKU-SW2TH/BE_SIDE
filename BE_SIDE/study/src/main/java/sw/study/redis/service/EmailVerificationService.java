package sw.study.redis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class EmailVerificationService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 토큰 저장 메서드
    public void saveVerificationToken(String email, String token) {
        redisTemplate.opsForValue().set(email, token);
        redisTemplate.expire(email, 10, TimeUnit.MINUTES);  // 토큰 만료 시간 설정 (10분)
    }

    // 토큰 조회 및 검증 메서드
    public boolean verifyToken(String email, String token) {
        String storedToken = (String) redisTemplate.opsForValue().get(email);
        return token.equals(storedToken);
    }
}
