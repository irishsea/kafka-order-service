package sergeeva.dev.kafka_order_service.locking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import sergeeva.dev.kafka_order_service.api.product.ProductDto;
import sergeeva.dev.kafka_order_service.api.product.ProductDtoMapper;
import sergeeva.dev.kafka_order_service.api.product.ProductUpdateRequest;
import sergeeva.dev.kafka_order_service.domain.db.ProductEntity;
import sergeeva.dev.kafka_order_service.domain.service.DbProductService;

class ProductUpdateLockingControllerTest {

    @Mock
    private RedisLockManager redisLockManager;

    @Mock
    private DbProductService dbProductService;

    @Mock
    private ProductDtoMapper mapper;

    private ProductUpdateLockingController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new ProductUpdateLockingController(redisLockManager, dbProductService, mapper);
    }

    @Test
    void returnsLockedWhenLockCannotBeAcquired() {
        when(redisLockManager.tryLock(eq("product:1"), eq(Duration.ofMinutes(1)))).thenReturn(null);

        Throwable thrown = catchThrowable(() -> controller.update(1L, new ProductUpdateRequest(BigDecimal.ONE, "x"), 0L));
        assertThat(thrown).isInstanceOf(ResponseStatusException.class);
        ResponseStatusException response = (ResponseStatusException) thrown;
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.LOCKED);

        verify(dbProductService, never()).update(any(), any());
        verify(redisLockManager, never()).unlock(any(), any());
    }

    @Test
    void returnsNotFoundAndUnlocksWhenEntityMissing() {
        when(redisLockManager.tryLock(eq("product:2"), eq(Duration.ofMinutes(1)))).thenReturn("lock-id");
        when(dbProductService.update(2L, new ProductUpdateRequest(BigDecimal.ONE, "x"))).thenReturn(null);

        ResponseEntity<?> response = controller.update(2L, new ProductUpdateRequest(BigDecimal.ONE, "x"), 0L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(redisLockManager).unlock("product:2", "lock-id");
    }

    @Test
    void updatesAndReturnsDtoWhenLockIsAcquired() {
        var request = new ProductUpdateRequest(new BigDecimal("99.99"), "Updated");
        var entity = productEntity(3L, "Desk", new BigDecimal("99.99"), "Updated");
        var dto = new ProductDto(3L, "Desk", new BigDecimal("99.99"), "Updated", null, null);

        when(redisLockManager.tryLock(eq("product:3"), eq(Duration.ofMinutes(1)))).thenReturn("lock-id");
        when(dbProductService.update(3L, request)).thenReturn(entity);
        when(mapper.toProductDto(entity)).thenReturn(dto);

        ResponseEntity<?> response = controller.update(3L, request, 0L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dto);
        verify(redisLockManager).unlock("product:3", "lock-id");
    }

    private static ProductEntity productEntity(Long id, String name, BigDecimal price, String description) {
        return ProductEntity.builder()
                .id(id)
                .name(name)
                .price(price)
                .description(description)
                .build();
    }
}
