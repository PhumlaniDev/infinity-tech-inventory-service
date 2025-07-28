package com.phumlanidev.inventoryservice.exception.inventory;

import com.phumlanidev.inventoryservice.exception.BaseException;
import org.springframework.http.HttpStatus;

public class InsufficientStockException extends BaseException {


  public InsufficientStockException(String message) {
    super(message, HttpStatus.BAD_REQUEST);
  }
}
