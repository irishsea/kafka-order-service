package sergeeva.dev.kafka_order_service.domain.service;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import sergeeva.dev.kafka_order_service.domain.db.OrderEntity;

class OrderServiceTest {

    @Mock
    private OrderKafkaProducer orderKafkaProducer;

    private OrderService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new OrderService(orderKafkaProducer);
    }

    @Test
    void saveOrderDelegatesToKafkaProducer() {
        var order = new OrderEntity(7L, "Mouse", 1);

        service.saveOrder(order);

        verify(orderKafkaProducer).sendOrderToKafka(order);
    }
}
