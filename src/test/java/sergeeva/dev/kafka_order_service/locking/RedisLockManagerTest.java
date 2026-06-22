package sergeeva.dev.kafka_order_service.locking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings({"rawtypes", "unchecked"})
class RedisLockManagerTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations valueOperations;

    private RedisLockManager lockManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        lockManager = new RedisLockManager(stringRedisTemplate);
    }

    @Test
    void unlockDelegatesToRedisScriptExecution() {
        lockManager.unlock("product:1", "lock-id");

        verify(stringRedisTemplate).execute(any(), eq(true));
    }
}
