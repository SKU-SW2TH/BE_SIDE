package sw.study.user.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisTemplate<String, Object> redisBlackListTemplate;

    // 값 저장 및 만료 시간 설정
    public void setData(String key, String value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    public void setBlackList(String key, boolean value, long timeout, TimeUnit unit) {
        redisBlackListTemplate.opsForValue().set(key, value, timeout, unit);
    }

    // 값 조회
    public String getData(String key) {
        return (String) redisTemplate.opsForValue().get(key);
    }

    public Object getBlackList(String key) {
        return redisBlackListTemplate.opsForValue().get(key);
    }

    // 키 삭제
    public boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    public boolean deleteBlackList(String key) {
        return redisBlackListTemplate.delete(key);
    }

    // 키 존재 여부 확인
    public boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    public boolean hasKeyBlackList(String key) {
        return redisBlackListTemplate.hasKey(key);
    }

    // 만료 시간 설정
    public boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    public void flushAll(){
        redisBlackListTemplate.getConnectionFactory().getConnection().flushDb();
        redisTemplate.getConnectionFactory().getConnection().flushDb();
    }

}
