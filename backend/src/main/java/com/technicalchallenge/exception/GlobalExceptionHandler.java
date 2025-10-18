package com.technicalchallenge.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<String> handleValidationExceptions(MethodArgumentNotValidException e) {
    var fieldErrors = e.getBindingResult().getFieldErrors();
    String message;
    boolean isBookNameMissing = fieldErrors.stream()
        .anyMatch(error -> "bookName".equals(error.getField()));
    if (isBookNameMissing) {
      message = "Book and Counterparty are required";
    } else {
      message = fieldErrors.get(0).getDefaultMessage();
    }
    return ResponseEntity.badRequest().body(message);
  }
}
