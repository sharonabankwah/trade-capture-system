package com.technicalchallenge.exception;

/**
 * Custom exception thrown when a user attempts to perform an operation
 * they do not have sufficient privileges for
 */
public class UserPrivilegeValidationException extends RuntimeException {

  /**
   * Constructs a new UserPrivilegeValidationException with the specified detail message.
   * @param message a description of why the privilege validation failed
   */
  public UserPrivilegeValidationException(String message) {
      super(message);
  }

  /**
   * Constructs a new UserPrivilegeValidationException with the specified detail message
   * and cause
   * @param message a description of why the privilege validation failed
   * @param cause   the underlying cause of the exception
   */
  public UserPrivilegeValidationException(String message, Throwable cause) {
      super(message, cause);
  }
}