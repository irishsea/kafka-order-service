package sergeeva.dev.kafka_order_service.api.product;

import java.math.BigDecimal;

public record ProductUpdateRequest (BigDecimal price,
                                    String description) {
}
