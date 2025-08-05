package com.phumlanidev.inventoryservice.config;

import com.phumlanidev.commonevents.events.OrderPlacedEvent;
import com.phumlanidev.commonevents.events.ProductCreatedEvent;
import com.phumlanidev.commonevents.events.StockReservationFailedEvent;
import com.phumlanidev.commonevents.events.StockReservedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

  private final static String bootstrapServers = "localhost:29092";

  // --- CONSUMER CONFIG (for OrderPlacedEvent) ---
  @Bean
  public ConsumerFactory<String, OrderPlacedEvent> orderPlacedEventConsumerFactory() {
    Map<String, Object> config = new HashMap<>();
    config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    config.put(ConsumerConfig.GROUP_ID_CONFIG, "inventory-group");
    config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
    config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
    config.put("spring.deserializer.key.delegate.class", StringDeserializer.class);
    config.put("spring.deserializer.value.delegate.class", JsonDeserializer.class);
    config.put(JsonDeserializer.TRUSTED_PACKAGES, "com.phumlanidev.commonevents.events");
    config.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, true);
    config.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.phumlanidev.commonevents.events.OrderPlacedEvent");
//    config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

    return new DefaultKafkaConsumerFactory<>(config);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, OrderPlacedEvent> orderKafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, OrderPlacedEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(orderPlacedEventConsumerFactory());
    factory.setConcurrency(3);
    factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
    factory.setCommonErrorHandler(new DefaultErrorHandler());
    return factory;
  }

  @Bean
  public ConsumerFactory<String, ProductCreatedEvent> productCreatedEventConsumerFactory() {
    Map<String, Object> config = new HashMap<>();
    config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    config.put(ConsumerConfig.GROUP_ID_CONFIG, "inventory-group");
    config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
    config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
    config.put("spring.deserializer.key.delegate.class", StringDeserializer.class);
    config.put("spring.deserializer.value.delegate.class", JsonDeserializer.class);
    config.put(JsonDeserializer.TRUSTED_PACKAGES, "com.phumlanidev.commonevents.events");
    config.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, true);
    config.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.phumlanidev.commonevents.events.ProductCreatedEvent");
    return new DefaultKafkaConsumerFactory<>(config);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, ProductCreatedEvent>
  productCreatedEventKafkaListenerContainerFactory() {
    var factory = new ConcurrentKafkaListenerContainerFactory<String, ProductCreatedEvent>();
    factory.setConsumerFactory(productCreatedEventConsumerFactory());
    factory.setConcurrency(3);
    factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
    factory.setCommonErrorHandler(new DefaultErrorHandler());
    return factory;
  }

  @Bean
  public ProducerFactory<String, StockReservedEvent> stockReservedEventProducerFactory() {
    Map<String, Object> props = new HashMap<>();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, true); // for proper deserialization
    return new DefaultKafkaProducerFactory<>(props);
  }

  @Bean
  public ProducerFactory<String, StockReservationFailedEvent> stockReservationFailedEventProducerFactory() {
    Map<String, Object> props = new HashMap<>();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, true);
    return new DefaultKafkaProducerFactory<>(props);
  }

  private <T> ProducerFactory<String, T> producerFactory() {
    Map<String, Object> props = new HashMap<>();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, true);
    return new DefaultKafkaProducerFactory<>(props);
  }

  @Bean
  public KafkaTemplate<String, OrderPlacedEvent> orderPlacedEventKafkaTemplate() {
    return new KafkaTemplate<>(producerFactory());
  }

  @Bean
  public KafkaTemplate<String, StockReservedEvent> stockReservedEventKafkaTemplate() {
    return new KafkaTemplate<>(producerFactory());
  }

  @Bean
  public KafkaTemplate<String, StockReservationFailedEvent> stockReservationFailedEventKafkaTemplate() {
    return new KafkaTemplate<>(producerFactory());
  }

  @Bean
  public KafkaTemplate<String, ProductCreatedEvent> productCreatedEventKafkaTemplate() {
    return new KafkaTemplate<>(producerFactory());
  }
}