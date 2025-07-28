package com.phumlanidev.inventoryservice.config;

import com.phumlanidev.commonevents.events.OrderPlacedEvent;
import com.phumlanidev.commonevents.events.ProductCreatedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

  // --- CONSUMER CONFIG (for OrderPlacedEvent) ---
  @Bean
  public ConsumerFactory<String, OrderPlacedEvent> orderConsumerFactory() {
    Map<String, Object> config = new HashMap<>();
    config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    config.put(ConsumerConfig.GROUP_ID_CONFIG, "inventory-group");
    config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
    config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

    return new DefaultKafkaConsumerFactory<>(config);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, OrderPlacedEvent> orderKafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, OrderPlacedEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(orderConsumerFactory());
    return factory;
  }

  // --- PRODUCER CONFIG FOR OrderPlacedEvent (optional if needed elsewhere) ---
  @Bean
  public ProducerFactory<String, OrderPlacedEvent> orderProducerFactory() {
    Map<String, Object> config = new HashMap<>();
    config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    return new DefaultKafkaProducerFactory<>(config);
  }

  @Bean
  public KafkaTemplate<String, OrderPlacedEvent> orderKafkaTemplate() {
    return new KafkaTemplate<>(orderProducerFactory());
  }

  // --- PRODUCER CONFIG FOR ProductCreatedEvent (❗ REQUIRED FOR DLQ USE) ---
  @Bean
  public ProducerFactory<String, ProductCreatedEvent> productCreatedProducerFactory() {
    Map<String, Object> config = new HashMap<>();
    config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    return new DefaultKafkaProducerFactory<>(config);
  }

  @Bean
  public ConsumerFactory<String, ProductCreatedEvent> productCreatedConsumerFactory() {
    JsonDeserializer<ProductCreatedEvent> deserializer = new JsonDeserializer<>(ProductCreatedEvent.class);
    deserializer.addTrustedPackages("*");

    return new DefaultKafkaConsumerFactory<>(
            Map.of(
                    ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092",
                    ConsumerConfig.GROUP_ID_CONFIG, "inventory-group",
                    ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"
            ),
            new StringDeserializer(),
            deserializer
    );
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, ProductCreatedEvent> productCreatedKafkaListenerContainerFactory() {
    var factory = new ConcurrentKafkaListenerContainerFactory<String, ProductCreatedEvent>();
    factory.setConsumerFactory(productCreatedConsumerFactory());
    return factory;
  }


  @Bean
  public KafkaTemplate<String, ProductCreatedEvent> productCreatedKafkaTemplate() {
    return new KafkaTemplate<>(productCreatedProducerFactory());
  }
}
