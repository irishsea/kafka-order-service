package sergeeva.dev.kafka_order_service.api.product;

import java.math.BigDecimal;

public record ProductCreateRequest (String name,
                                    BigDecimal price,
                                    String description) {
}
