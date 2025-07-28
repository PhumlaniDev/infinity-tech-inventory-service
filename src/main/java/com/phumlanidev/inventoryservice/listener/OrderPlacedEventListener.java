package com.phumlanidev.inventoryservice.listener;


import com.phumlanidev.commonevents.events.OrderPlacedEvent;
import com.phumlanidev.commonevents.events.StockReservationFailedEvent;
import com.phumlanidev.commonevents.events.StockReservedEvent;
import com.phumlanidev.inventoryservice.dto.StockRequestDto;
import com.phumlanidev.inventoryservice.service.impl.InventoryServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Component
@Slf4j
public class OrderPlacedEventListener extends BaseEventListener<OrderPlacedEvent>{

  private final InventoryServiceImpl inventoryService;
  private final StreamBridge streamBridge;

  public OrderPlacedEventListener(
          KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate, InventoryServiceImpl inventoryService,
          StreamBridge streamBridge) {
    super(kafkaTemplate, "order-placed-dlq");
    this.inventoryService = inventoryService;
    this.streamBridge = streamBridge;
  }

  @Bean
  public Consumer<OrderPlacedEvent> onOrderPlaced() {
    return event -> processWithDlq(event.getOrderId().toString(), event, () ->{
      log.info("📦 Inventory reserving for order: {}", event.getOrderId());

      List<StockRequestDto> reserved = new ArrayList<>();
      try {
        for (OrderPlacedEvent.OrderItemDto item : event.getItems()) {
          StockRequestDto stockRequestDto = StockRequestDto.builder()
                  .productId(item.getProductId())
                  .quantity(item.getQuantity())
                  .build();

          inventoryService.reserveStock(stockRequestDto);
          reserved.add(stockRequestDto);
          log.info("✅ Reserved {} units of product ID: {}", item.getQuantity(), item.getProductId());
        }
        streamBridge.send("stockReserved-out-0", new StockReservedEvent(
                event.getOrderId(), event.getUserId(), event.getItems(), Instant.now()
        ));
      } catch (Exception e) {
        log.error("❌ Stock reservation failed. Releasing previously reserved stock.", e);

        reserved.forEach(inventoryService::releaseStock);

        streamBridge.send("stockReservationFailed-out-0", new StockReservationFailedEvent(
                event.getOrderId(), event.getUserId(),
                "Stock reservation failed. " + e.getMessage(),
                Instant.now()
        ));
      }
    });
  }
}
