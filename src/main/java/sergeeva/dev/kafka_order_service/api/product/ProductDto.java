package sergeeva.dev.kafka_order_service.api.product;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO for {@link ProductEntity}
 */
public record ProductDto (Long id,
                          String name,
                          BigDecimal price,
                          String description,
                          Instant createdAt,
                          Instant updatedAt) {
}
