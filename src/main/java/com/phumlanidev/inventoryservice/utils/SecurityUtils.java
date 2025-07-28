package com.phumlanidev.inventoryservice.utils;

import com.phumlanidev.inventoryservice.config.JwtAuthenticationConverter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

  private final JwtAuthenticationConverter jwtAuthenticationConverter;
  private final HttpServletRequest request;

  public String getCurrentToken() {
    String token = request.getHeader("Authorization");
    if (token != null && token.startsWith("Bearer ")) {
      return token.substring(7); // Remove "Bearer " prefix
    }
    throw new IllegalArgumentException("Invalid or missing Authorization token"); // or throw an exception if you prefer
  }

  public String getCurrentUserId() {
    return jwtAuthenticationConverter.getCurrentUserId();
  }

  public String getCurrentEmail() {
    return jwtAuthenticationConverter.getCurrentEmail();
  }

  public String getCurrentClientIp() {
    return request.getRemoteAddr();
  }

  public String getCurrentJwtToken() {
    return jwtAuthenticationConverter.getCurrentJwt().getTokenValue();
  }

  public String getCurrentUsername() {
    return jwtAuthenticationConverter.getCurrentUsername();
  }
}
