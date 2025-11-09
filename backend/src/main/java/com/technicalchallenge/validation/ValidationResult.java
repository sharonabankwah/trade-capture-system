package com.technicalchallenge.validation;

import java.util.ArrayList;
import java.util.List;

public class ValidationResult {

  private boolean valid;
  private List<String> errors;

  public ValidationResult() {
      // Defaults to valid
      this.valid = true;
      this.errors = new ArrayList<>();
  }

  // Checks if the validation passed or failed
  public boolean isValid() {
      return valid;
  }

  // Returns all error messages
  public List<String> getErrors() {
      return errors;
  }

  // Checks if any validation errors exist
  public boolean hasErrors() {
    return !errors.isEmpty();
  }

  // Adds an error message
  public void addError(String errorMessage) {
      this.errors.add(errorMessage);
      // Once an error has been added, mark invalid
      this.valid = false; 
  }

  // Stores all error messages
  public void addErrors(List<String> errorMessages) {
      if (errorMessages != null && !errorMessages.isEmpty()) {
          this.errors.addAll(errorMessages);
          this.valid = false;
      }
  }

  public void merge(ValidationResult other) {
      if (other != null && !other.isValid()) {
          this.addErrors(other.getErrors());
      }
  }

  @Override
  public String toString() {
      return valid ? "Validation successful" : String.join("; ", errors);
  }
}