package sergeeva.dev.kafka_order_service.domain.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import sergeeva.dev.kafka_order_service.domain.db.OrderEntity;

@Service
public class OrderKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderKafkaProducer.class);

    private final KafkaTemplate<String, OrderEntity> kafkaTemplate;

    public OrderKafkaProducer(KafkaTemplate<String, OrderEntity> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendOrderToKafka(OrderEntity order) {
        kafkaTemplate.send("orders", order.orderId().toString(), order);
        log.info("Order sent to kafka: id={}", order.orderId());
    }

}