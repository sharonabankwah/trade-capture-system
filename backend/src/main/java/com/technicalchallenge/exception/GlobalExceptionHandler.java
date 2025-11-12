package com.technicalchallenge.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Handles global exceptions for controller methods
 */
@ControllerAdvice
public class GlobalExceptionHandler {

  /**
     * Handles validation errors from @Valid annotated request bodies
     * @param e the exception containing validation details
     * @return a BAD_REQUEST response with a user-friendly message
     */
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<String> handleValidationExceptions(MethodArgumentNotValidException e) {

    var fieldErrors = e.getBindingResult().getFieldErrors();

    // Default message based on first validation error
    String message;

    // Checks if the "bookName" field is missing and customises message
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
