package com.campusride.common.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(EmailAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> handleEmailAlreadyExists(
      EmailAlreadyExistsException ex, HttpServletRequest request) {
    return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    String message =
        ex.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .orElse("Validation failed");

    return buildErrorResponse(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
  }

  @ExceptionHandler(RideNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleRideNotFound(
      RideNotFoundException ex, HttpServletRequest request) {
    return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
  }

  @ExceptionHandler(RideAccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handleRideAccessDenied(
      RideAccessDeniedException ex, HttpServletRequest request) {

    return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request.getRequestURI());
  }

  @ExceptionHandler(RideAlreadyCancelledException.class)
  public ResponseEntity<ErrorResponse> handleRideAlreadyCancelled(
      RideAlreadyCancelledException ex, HttpServletRequest request) {
    return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneralException(
      Exception ex, HttpServletRequest request) {
    return buildErrorResponse(
        HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error", request.getRequestURI());
  }

  private ResponseEntity<ErrorResponse> buildErrorResponse(
      HttpStatus status, String message, String path) {
    ErrorResponse response =
        new ErrorResponse(
            LocalDateTime.now(), status.value(), status.getReasonPhrase(), message, path);

    return ResponseEntity.status(status).body(response);
  }
}
