package sergeeva.dev.kafka_order_service.api.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.FilterChain;

class RateLimitFilterTest {

    @Mock
    private FixedWindowRateLimiter fixedWindowRateLimiter;

    @Mock
    private FilterChain filterChain;

    private RateLimitFilter filter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        filter = new RateLimitFilter(fixedWindowRateLimiter);
    }

    @Test
    void usesApiKeyHeaderWhenPresent() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader("X-API-KEY", "api-key-1");
        var response = new MockHttpServletResponse();

        when(fixedWindowRateLimiter.allowRequest(eq("api-key-1"), eq(10), eq(Duration.ofMinutes(1)))).thenReturn(true);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void fallsBackToRemoteAddressAndBlocksWhenOverLimit() throws Exception {
        var request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        var response = new MockHttpServletResponse();

        when(fixedWindowRateLimiter.allowRequest(eq("127.0.0.1"), eq(10), eq(Duration.ofMinutes(1)))).thenReturn(false);

        filter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(429);
        assertThat(response.getContentAsString()).isEqualTo("Rate limit exceeded");
    }
}
