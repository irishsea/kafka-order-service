package sergeeva.dev.kafka_order_service.locking;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.Uuid;
import org.jspecify.annotations.NonNull;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Slf4j
@Service
@AllArgsConstructor
public class RedisLockManager {

    private static final String RELEASE_LOCK_LUA_SCRIPT = """
            if redis.call('GET', KEYS[1]) == ARGV[1] then return redis.call('DEL', KEYS[1])
            else return 0 end
            """;

    private final StringRedisTemplate stringRedisTemplate;

    public String tryLock(
            String key,
            Duration ttl
    ) {
        String lockKey = buildLockKey(key);
        String lockId = Uuid.randomUuid().toString();

        Boolean isLockedSuccessful = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, lockId, ttl);

        if (Boolean.TRUE.equals(isLockedSuccessful)) {
            log.info("Lock has been acquired for: lockKey {}, lockId {}", lockKey, lockId);
            return lockId;
        }
        return null;
    }

    public void unlock(
            String key,
            String lockId
    ) {
        String lockKey = buildLockKey(key);
        log.info("Trying to unlock with: lockKey {}, lockId {}", lockKey, lockId);

        Long result = stringRedisTemplate.execute(connection -> connection.scriptingCommands().eval(
                RELEASE_LOCK_LUA_SCRIPT.getBytes(StandardCharsets.UTF_8),
                ReturnType.INTEGER,
                1,
                lockKey.getBytes(StandardCharsets.UTF_8),
                lockId.getBytes(StandardCharsets.UTF_8)

        ), true);

        if (result != null && result == 1) {
            log.info("Lock has been released for: lockKey {}, lockId {}", lockKey, lockId);
        } else {
            log.info("Lock hasn't been released or re-acquired for: lockKey {}, lockId {}", lockKey, lockId);
        }
    }

    private static @NonNull String buildLockKey(String key) {
        String lockKey = "lock:" + key;
        return lockKey;
    }
}
