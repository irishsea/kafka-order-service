package sergeeva.dev.kafka_order_service.domain;

import sergeeva.dev.kafka_order_service.api.product.ProductCreateRequest;
import sergeeva.dev.kafka_order_service.api.product.ProductUpdateRequest;
import sergeeva.dev.kafka_order_service.domain.db.ProductEntity;

public interface ProductService {

    ProductEntity create(ProductCreateRequest createRequest);

    ProductEntity update(Long id, ProductUpdateRequest updateRequest);

    ProductEntity getById(Long id);

    void delete(Long id);
}
