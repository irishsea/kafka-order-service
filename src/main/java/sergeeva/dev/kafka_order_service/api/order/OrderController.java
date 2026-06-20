//package sergeeva.dev.kafka_order_service.api.order;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import sergeeva.dev.kafka_order_service.domain.db.OrderEntity;
//import sergeeva.dev.kafka_order_service.domain.service.OrderService;
//
//import java.util.concurrent.ThreadLocalRandom;
//import java.util.concurrent.atomic.AtomicInteger;
//
//@RestController
//@RequestMapping("/orders")
//public class OrderController {
//
//    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
//
//    private final OrderService orderService;
//    private final AtomicInteger orderIdCounter;
//
//    public OrderController(OrderService orderService) {
//        this.orderService = orderService;
//        orderIdCounter = new AtomicInteger();
//    }
//
//    @PostMapping
//    public void createOrder(@RequestBody OrderEntity order) {
//        long orderId = orderIdCounter.incrementAndGet();
//        var productName = order.product() + ThreadLocalRandom.current().nextInt(100);
//
//        orderService.saveOrder(new OrderEntity(orderId, productName, order.quantity()));
//        logger.info("Create order called: order{}", order);
//    }
//}
