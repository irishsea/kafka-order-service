package sergeeva.dev.kafka_order_service.api.product;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sergeeva.dev.kafka_order_service.domain.db.ProductEntity;
import sergeeva.dev.kafka_order_service.domain.service.DbProductService;

@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {


    private final DbProductService dbProductService;
    //    private final ManualCachingProductService manualCachingProductService;
    //    private final SpringAnnotationCachingProductService springAnnotationCachingProductService;
    private final ProductDtoMapper mapper;

    public ResponseEntity<ProductDto> create(@RequestBody ProductCreateRequest request) {
        log.info("Creating product");

        ProductEntity product = dbProductService.create(request);
        ProductDto dto = mapper.toProductDto(product);

        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> update(@PathVariable Long id,
                                             @RequestBody ProductUpdateRequest request) {
        log.info("Updating product {}", id);
        ProductEntity product = dbProductService.update(id, request);
        ProductDto dto = mapper.toProductDto(product);

        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ProductDto> delete(@PathVariable Long id) {
        log.info("Deleting product {}", id);
        dbProductService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getById(
            @PathVariable Long id
    ) {
        log.info("Getting product {}", id);

        ProductEntity product = dbProductService.getById(id);
        ProductDto dto = mapper.toProductDto(product);

        return ResponseEntity.ok(dto);
    }
}
