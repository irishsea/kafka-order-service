package sergeeva.dev.kafka_order_service.domain.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;

import sergeeva.dev.kafka_order_service.domain.db.OrderEntity;

@SuppressWarnings({"rawtypes", "unchecked"})
class OrderKafkaProducerTest {

    @Mock
    private KafkaTemplate kafkaTemplate;

    private OrderKafkaProducer producer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        producer = new OrderKafkaProducer(kafkaTemplate);
    }

    @Test
    void sendsOrderToOrdersTopicWithOrderIdAsKey() {
        var order = new OrderEntity(42L, "Keyboard", 3);

        producer.sendOrderToKafka(order);

        verify(kafkaTemplate).send("orders", "42", order);
    }
}
