package sergeeva.dev.kafka_order_service.api.product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import sergeeva.dev.kafka_order_service.domain.db.ProductEntity;
import sergeeva.dev.kafka_order_service.domain.service.CacheMode;
import sergeeva.dev.kafka_order_service.domain.service.DbProductService;
import sergeeva.dev.kafka_order_service.domain.service.ManualCachingProductService;
import sergeeva.dev.kafka_order_service.domain.service.SpringAnnotationCachingProductService;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductControllerTest {

    @Mock
    private DbProductService dbProductService;

    @Mock
    private ManualCachingProductService manualCachingProductService;

    @Mock
    private SpringAnnotationCachingProductService springAnnotationCachingProductService;

    @Mock
    private ProductDtoMapper mapper;

    private ProductController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new ProductController(
                dbProductService,
                manualCachingProductService,
                springAnnotationCachingProductService,
                mapper
        );
    }

    @Test
    void createUsesDbServiceWhenCacheModeIsNoneCache() {
        var request = new ProductCreateRequest("Keyboard", new BigDecimal("199.99"), "Mechanical");
        var entity = productEntity(1L, "Keyboard", new BigDecimal("199.99"), "Mechanical");
        var dto = productDto(1L, "Keyboard", new BigDecimal("199.99"), "Mechanical");

        when(dbProductService.create(request)).thenReturn(entity);
        when(mapper.toProductDto(entity)).thenReturn(dto);

        ResponseEntity<?> response = controller.create(request, CacheMode.NONE_CACHE);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(dto);
        verify(dbProductService).create(request);
    }

    @Test
    void createUsesManualCacheServiceWhenRequested() {
        var request = new ProductCreateRequest("Mouse", new BigDecimal("49.99"), "Wireless");
        var entity = productEntity(2L, "Mouse", new BigDecimal("49.99"), "Wireless");
        var dto = productDto(2L, "Mouse", new BigDecimal("49.99"), "Wireless");

        when(manualCachingProductService.create(request)).thenReturn(entity);
        when(mapper.toProductDto(entity)).thenReturn(dto);

        ResponseEntity<?> response = controller.create(request, CacheMode.MANUAL);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(dto);
        verify(manualCachingProductService).create(request);
    }

    @Test
    void getByIdReturnsNotFoundWhenServiceReturnsNull() {
        when(springAnnotationCachingProductService.getById(10L)).thenReturn(null);

        ResponseEntity<?> response = controller.getById(10L, CacheMode.SPRING);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.hasBody()).isFalse();
        verify(springAnnotationCachingProductService).getById(10L);
    }

    @Test
    void updateReturnsMappedDto() {
        var request = new ProductUpdateRequest(new BigDecimal("89.50"), "Updated");
        var entity = productEntity(11L, "Headphones", new BigDecimal("89.50"), "Updated");
        var dto = productDto(11L, "Headphones", new BigDecimal("89.50"), "Updated");

        when(dbProductService.update(11L, request)).thenReturn(entity);
        when(mapper.toProductDto(entity)).thenReturn(dto);

        ResponseEntity<?> response = controller.update(11L, request, CacheMode.NONE_CACHE);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dto);
        verify(dbProductService).update(11L, request);
    }

    @Test
    void deleteReturnsNoContent() {
        ResponseEntity<?> response = controller.delete(77L, CacheMode.MANUAL);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.hasBody()).isFalse();
        verify(manualCachingProductService).delete(77L);
    }

    private static ProductEntity productEntity(Long id, String name, BigDecimal price, String description) {
        return ProductEntity.builder()
                .id(id)
                .name(name)
                .price(price)
                .description(description)
                .build();
    }

    private static ProductDto productDto(Long id, String name, BigDecimal price, String description) {
        return new ProductDto(id, name, price, description, null, null);
    }
}
