package sergeeva.dev.kafka_order_service.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import sergeeva.dev.kafka_order_service.api.product.ProductCreateRequest;
import sergeeva.dev.kafka_order_service.api.product.ProductUpdateRequest;
import sergeeva.dev.kafka_order_service.domain.db.ProductEntity;
import sergeeva.dev.kafka_order_service.domain.db.ProductRepository;

@SuppressWarnings({"rawtypes", "unchecked"})
class ManualCachingProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private RedisTemplate redisTemplate;

    @Mock
    private ValueOperations valueOperations;

    private ManualCachingProductService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        service = new ManualCachingProductService(productRepository, redisTemplate);
    }

    @Test
    void getByIdReturnsCachedValueAndSkipsRepository() {
        var cached = productEntity(1L, "Keyboard", new BigDecimal("199.99"), "Cached");
        when(valueOperations.get("product:1")).thenReturn(cached);

        ProductEntity result = service.getById(1L);

        assertThat(result).isSameAs(cached);
        verify(productRepository, never()).findById(any());
    }

    @Test
    void getByIdLoadsFromRepositoryAndCachesResultOnMiss() {
        var entity = productEntity(2L, "Mouse", new BigDecimal("49.99"), "Fresh");
        when(valueOperations.get("product:2")).thenReturn(null);
        when(productRepository.findById(2L)).thenReturn(Optional.of(entity));

        ProductEntity result = service.getById(2L);

        assertThat(result).isSameAs(entity);
        verify(valueOperations).set(eq("product:2"), eq(entity), eq(Duration.ofMinutes(1)));
    }

    @Test
    void updateInvalidatesCacheAfterSaving() {
        var existing = productEntity(3L, "Headphones", new BigDecimal("79.99"), "Old");
        when(productRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(ProductEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductEntity result = service.update(3L, new ProductUpdateRequest(new BigDecimal("89.99"), "New"));

        assertThat(result.getPrice()).isEqualByComparingTo("89.99");
        assertThat(result.getDescription()).isEqualTo("New");
        verify(redisTemplate).delete("product:3");
    }

    @Test
    void deleteInvalidatesCache() {
        when(productRepository.existsById(4L)).thenReturn(true);

        service.delete(4L);

        verify(productRepository).deleteById(4L);
        verify(redisTemplate).delete("product:4");
    }

    @Test
    void createDelegatesToRepository() {
        var request = new ProductCreateRequest("Desk", new BigDecimal("299.00"), "Wood");
        var saved = productEntity(5L, "Desk", new BigDecimal("299.00"), "Wood");
        when(productRepository.save(any(ProductEntity.class))).thenReturn(saved);

        ProductEntity result = service.create(request);

        assertThat(result).isSameAs(saved);
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
