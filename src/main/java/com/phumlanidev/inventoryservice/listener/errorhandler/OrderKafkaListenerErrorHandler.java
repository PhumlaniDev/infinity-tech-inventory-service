package com.phumlanidev.inventoryservice.listener.errorhandler;

import com.phumlanidev.commonevents.events.OrderPlacedEvent;
import com.phumlanidev.inventoryservice.listener.dlq.OrderPlacedEventDlqPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.KafkaListenerErrorHandler;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component("orderKafkaErrorHandler")
@RequiredArgsConstructor
@Slf4j
public class OrderKafkaListenerErrorHandler implements KafkaListenerErrorHandler {

  private final OrderPlacedEventDlqPublisher dlqPublisher;

  @Override
  public Object handleError(Message<?> message, ListenerExecutionFailedException exception) {
    try {
      ConsumerRecord<String, OrderPlacedEvent> record = (ConsumerRecord<String, OrderPlacedEvent>) message.getPayload();
      dlqPublisher.publishToDlq(record.key(), record.value(), exception);
    } catch (Exception e) {
      log.error("Failed tp handle OrderPlacedEvent error: {}", e.getMessage(), e);
    }
    return null;
  }
}
