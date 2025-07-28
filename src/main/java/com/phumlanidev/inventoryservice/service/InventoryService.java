package com.phumlanidev.inventoryservice.service;

import com.phumlanidev.inventoryservice.dto.StockRequestDto;
import com.phumlanidev.inventoryservice.dto.StockResponseDto;

public interface InventoryService {
  void reserveStock(StockRequestDto request);
  void releaseStock(StockRequestDto request);
  void deductStock(StockRequestDto request);
  StockResponseDto getStock(Long productId);
  void addStock(Long productId, int quantity);
}
