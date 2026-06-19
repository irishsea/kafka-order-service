package sergeeva.dev.kafka_order_service.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import sergeeva.dev.kafka_order_service.domain.db.OrderEntity;
import tools.jackson.databind.json.JsonMapper;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<String, OrderEntity> producerFactory(JsonMapper jsonMapper) {
        Map<String, Object> configProperties = new HashMap<>();
        configProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");


        JacksonJsonSerializer<OrderEntity> serializer = new JacksonJsonSerializer<>(jsonMapper);
        serializer.setAddTypeInfo(false);

        return new DefaultKafkaProducerFactory<>(
                configProperties,
                new StringSerializer(),
                serializer
        );
    }

    @Bean
    public KafkaTemplate<String, OrderEntity> kafkaTemplate(ProducerFactory<String, OrderEntity> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}