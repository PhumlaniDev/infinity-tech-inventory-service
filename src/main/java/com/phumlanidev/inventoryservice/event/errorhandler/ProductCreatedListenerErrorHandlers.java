package com.phumlanidev.inventoryservice.event.errorhandler;


import com.phumlanidev.commonevents.events.product.ProductCreatedEvent;
import com.phumlanidev.inventoryservice.event.dlq.ProductCreatedEventDlqPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.KafkaListenerErrorHandler;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component("productKafkaListenerErrorHandler")
@RequiredArgsConstructor
@Slf4j
public class ProductCreatedListenerErrorHandlers implements KafkaListenerErrorHandler {

  private final ProductCreatedEventDlqPublisher eventDlqPublisher;

  @Override
  public Object handleError(Message<?> message, ListenerExecutionFailedException exception) {
    try {
      ConsumerRecord<String, ProductCreatedEvent> record = (ConsumerRecord<String, ProductCreatedEvent>) message.getPayload();
      eventDlqPublisher.publishToDlq(record.key(), record.value(), exception);
    } catch (Exception e) {
      log.error("Failed to handle error properly: {}", e.getMessage(), e);
    }
    return null;
  }
}
