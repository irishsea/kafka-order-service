package sergeeva.dev.kafka_order_service.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import sergeeva.dev.kafka_order_service.api.product.ProductCreateRequest;
import sergeeva.dev.kafka_order_service.api.product.ProductUpdateRequest;
import sergeeva.dev.kafka_order_service.domain.db.ProductEntity;
import sergeeva.dev.kafka_order_service.domain.db.ProductRepository;

class SpringAnnotationCachingProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    private SpringAnnotationCachingProductService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new SpringAnnotationCachingProductService(productRepository);
    }

    @Test
    void createDelegatesToRepository() {
        var request = new ProductCreateRequest("Monitor", new BigDecimal("199.00"), "27 inch");
        var saved = productEntity(1L, "Monitor", new BigDecimal("199.00"), "27 inch");
        when(productRepository.save(any(ProductEntity.class))).thenReturn(saved);

        ProductEntity result = service.create(request);

        assertThat(result).isSameAs(saved);
    }

    @Test
    void getByIdReturnsEntityFromRepository() {
        var entity = productEntity(2L, "Keyboard", new BigDecimal("299.00"), "Mechanical");
        when(productRepository.findById(2L)).thenReturn(Optional.of(entity));

        ProductEntity result = service.getById(2L);

        assertThat(result).isSameAs(entity);
    }

    @Test
    void updateReturnsNullWhenMissing() {
        when(productRepository.findById(3L)).thenReturn(Optional.empty());

        assertThat(service.update(3L, new ProductUpdateRequest(new BigDecimal("10.00"), "x"))).isNull();
        verify(productRepository, never()).save(any());
    }

    @Test
    void deleteRemovesExistingEntityOnly() {
        when(productRepository.existsById(4L)).thenReturn(true);

        service.delete(4L);

        verify(productRepository).deleteById(4L);
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
