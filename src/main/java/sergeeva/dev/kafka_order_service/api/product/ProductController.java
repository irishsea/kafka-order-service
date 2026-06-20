package sergeeva.dev.kafka_order_service.api.product;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sergeeva.dev.kafka_order_service.domain.ProductService;
import sergeeva.dev.kafka_order_service.domain.db.ProductEntity;
import sergeeva.dev.kafka_order_service.domain.service.CacheMode;
import sergeeva.dev.kafka_order_service.domain.service.DbProductService;
import sergeeva.dev.kafka_order_service.domain.service.ManualCachingProductService;
import sergeeva.dev.kafka_order_service.domain.service.SpringAnnotationCachingProductService;


@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private static final String DEFAULT_CACHE_MODE = "NONE_CACHE";
    private static final String CACHE_MODE_PARAMETER = "cacheMode";

    private final DbProductService dbProductService;
    private final ManualCachingProductService manualCachingProductService;
    private final SpringAnnotationCachingProductService springAnnotationCachingProductService;
    private final ProductDtoMapper mapper;

    @PostMapping
    public ResponseEntity<ProductDto> create(
            @RequestBody ProductCreateRequest request,
            @RequestParam(value = CACHE_MODE_PARAMETER, defaultValue = DEFAULT_CACHE_MODE) CacheMode cacheMode
    ) {
        log.info("Creating product with cacheMode={}", cacheMode);

        ProductService service = resolveProductService(cacheMode);
        ProductEntity product = service.create(request);
        ProductDto dto = mapper.toProductDto(product);

        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getById(
            @PathVariable Long id,
            @RequestParam(value = CACHE_MODE_PARAMETER, defaultValue = DEFAULT_CACHE_MODE) CacheMode cacheMode
    ) {
        log.info("Getting product {} with cacheMode={}", id, cacheMode);

        ProductService service = resolveProductService(cacheMode);
        ProductEntity product = service.getById(id);

        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        ProductDto dto = mapper.toProductDto(product);

        return ResponseEntity.ok(dto);
    }


    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> update(
            @PathVariable Long id,
            @RequestBody ProductUpdateRequest request,
            @RequestParam(value = CACHE_MODE_PARAMETER, defaultValue = DEFAULT_CACHE_MODE) CacheMode cacheMode

    ) {
        log.info("Updating product {} with cacheMode={}", id, cacheMode);

        ProductService service = resolveProductService(cacheMode);
        ProductEntity product = service.update(id, request);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        ProductDto dto = mapper.toProductDto(product);

        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestParam(value = CACHE_MODE_PARAMETER, defaultValue = DEFAULT_CACHE_MODE) CacheMode cacheMode

    ) {
        log.info("Deleting product {} with cacheMode={}", id, cacheMode);

        ProductService service = resolveProductService(cacheMode);
        service.delete(id);

        return ResponseEntity.noContent().build();
    }

    private ProductService resolveProductService(CacheMode cacheMode) {
        return switch (cacheMode) {
            case NONE_CACHE -> dbProductService;
            case MANUAL -> manualCachingProductService;
            case SPRING -> springAnnotationCachingProductService;
        };
    }
}
