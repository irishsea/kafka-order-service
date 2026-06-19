package sergeeva.dev.kafka_order_service.domain.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import sergeeva.dev.kafka_order_service.domain.db.OrderEntity;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final OrderKafkaProducer orderKafkaProducer;

    public OrderService(OrderKafkaProducer orderKafkaProducer) {
        this.orderKafkaProducer = orderKafkaProducer;
    }

    public void saveOrder(OrderEntity order) {
        //saving to DB
        //send to kafka
        orderKafkaProducer.sendOrderToKafka(order);

        logger.info("Order successfully saved, id:{}", order.orderId());
    }
}
