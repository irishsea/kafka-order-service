package sergeeva.dev.kafka_order_service.domain.service;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sergeeva.dev.kafka_order_service.api.product.ProductCreateRequest;
import sergeeva.dev.kafka_order_service.api.product.ProductUpdateRequest;
import sergeeva.dev.kafka_order_service.domain.ProductService;
import sergeeva.dev.kafka_order_service.domain.db.ProductEntity;
import sergeeva.dev.kafka_order_service.domain.db.ProductRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class DbProductService implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public ProductEntity create(ProductCreateRequest createRequest) {
        log.info("Creating product in DB: {}", createRequest.name());
        ProductEntity product = ProductEntity.builder()
                .name(createRequest.name())
                .price(createRequest.price())
                .description(createRequest.description())
                .build();
        return productRepository.save(product);
    }

    @Override
    public ProductEntity update(Long id, ProductUpdateRequest updateRequest) {
        log.info("Creating product in DB: {}", id);
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (updateRequest.price() != null) {
            product.setPrice(updateRequest.price());
        }
        if (updateRequest.description() != null) {
            product.setDescription(updateRequest.description());
        }

        return productRepository.save(product);
    }

    @Override
    @Nullable
    public ProductEntity getById(Long id) {
        log.info("Getting product from DB: id={}", id);
        return productRepository.findById(id)
                .orElse(null);
    }

    @Override
    public void delete(Long id) {
        log.info("Deleting product from DB: {}", id);
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
        }
    }
}
