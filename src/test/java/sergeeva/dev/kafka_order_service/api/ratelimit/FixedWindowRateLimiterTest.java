package sergeeva.dev.kafka_order_service.api.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.types.Expiration;

@SuppressWarnings({"rawtypes", "unchecked"})
class FixedWindowRateLimiterTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations valueOperations;

    private FixedWindowRateLimiter limiter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        limiter = new FixedWindowRateLimiter(redisTemplate);
    }

    @Test
    void firstRequestCreatesExpiryAndIsAllowed() {
        when(valueOperations.increment(anyString())).thenReturn(1L);

        boolean allowed = limiter.allowRequest("client-a", 10, Duration.ofMinutes(1));

        assertThat(allowed).isTrue();
        verify(redisTemplate).expire(anyString(), eq(Duration.ofMinutes(1)));
    }

    @Test
    void requestAboveLimitIsRejected() {
        when(valueOperations.increment(anyString())).thenReturn(11L);

        boolean allowed = limiter.allowRequest("client-a", 10, Duration.ofMinutes(1));

        assertThat(allowed).isFalse();
        verify(redisTemplate, never()).expire(anyString(), (Expiration) any());
    }
}
