package sergeeva.dev.kafka_order_service.domain.db;

public record OrderEntity(
        Long orderId,
        String product,
        Integer quantity

) {
}
