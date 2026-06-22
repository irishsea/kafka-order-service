package sergeeva.dev.kafka_order_service.api.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;

@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int RATE_LIMIT = 10;
    private static final Duration WINDOW_SIZE = Duration.ofMinutes(1);

    private final FixedWindowRateLimiter fixedWindowRateLimiter;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String client = Optional.ofNullable(request.getHeader("X-API-KEY"))
                .filter(x -> !x.isBlank())
                .orElseGet(() -> Optional.ofNullable(request.getRemoteAddr()).orElse("unknown"));

        boolean allowed = fixedWindowRateLimiter.allowRequest(
                client,
                RATE_LIMIT,
                WINDOW_SIZE
        );

        if (!allowed) {
            response.setStatus(429);
            response.getWriter().write("Rate limit exceeded");
            return;
        }
        filterChain.doFilter(request, response);
    }
}
