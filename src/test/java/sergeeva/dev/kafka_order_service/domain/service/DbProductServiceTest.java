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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import sergeeva.dev.kafka_order_service.api.product.ProductCreateRequest;
import sergeeva.dev.kafka_order_service.api.product.ProductUpdateRequest;
import sergeeva.dev.kafka_order_service.domain.db.ProductEntity;
import sergeeva.dev.kafka_order_service.domain.db.ProductRepository;

class DbProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    private DbProductService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new DbProductService(productRepository);
    }

    @Test
    void createMapsRequestIntoEntityAndSavesIt() {
        var request = new ProductCreateRequest("Keyboard", new BigDecimal("199.99"), "Mechanical");
        var saved = productEntity(1L, "Keyboard", new BigDecimal("199.99"), "Mechanical");

        when(productRepository.save(any(ProductEntity.class))).thenReturn(saved);

        ProductEntity result = service.create(request);

        ArgumentCaptor<ProductEntity> captor = ArgumentCaptor.forClass(ProductEntity.class);
        verify(productRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Keyboard");
        assertThat(captor.getValue().getPrice()).isEqualByComparingTo("199.99");
        assertThat(captor.getValue().getDescription()).isEqualTo("Mechanical");
        assertThat(result).isSameAs(saved);
    }

    @Test
    void updateAppliesOnlyProvidedFields() {
        var existing = productEntity(10L, "Mouse", new BigDecimal("49.99"), "Wireless");
        var request = new ProductUpdateRequest(new BigDecimal("59.99"), null);

        when(productRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(ProductEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductEntity result = service.update(10L, request);

        assertThat(result.getName()).isEqualTo("Mouse");
        assertThat(result.getPrice()).isEqualByComparingTo("59.99");
        assertThat(result.getDescription()).isEqualTo("Wireless");
        verify(productRepository).save(existing);
    }

    @Test
    void updateReturnsNullWhenEntityNotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        ProductEntity result = service.update(99L, new ProductUpdateRequest(new BigDecimal("10.00"), "x"));

        assertThat(result).isNull();
        verify(productRepository, never()).save(any());
    }

    @Test
    void getByIdReturnsNullWhenMissing() {
        when(productRepository.findById(5L)).thenReturn(Optional.empty());

        assertThat(service.getById(5L)).isNull();
    }

    @Test
    void deleteRemovesExistingEntityOnly() {
        when(productRepository.existsById(7L)).thenReturn(true);

        service.delete(7L);

        verify(productRepository).deleteById(7L);
    }

    @Test
    void deleteDoesNothingWhenEntityMissing() {
        when(productRepository.existsById(7L)).thenReturn(false);

        service.delete(7L);

        verify(productRepository, never()).deleteById(any());
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
