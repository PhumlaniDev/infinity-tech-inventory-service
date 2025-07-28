package com.phumlanidev.inventoryservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StockResponseDto {
  private Long productId;
  private int available;
  private int reserved;
  private boolean success;
  private String message;
}
