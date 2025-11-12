package com.technicalchallenge.exception;

import java.util.List;

/**
 * Exception thrown when a trade fails validation
 */
public class TradeValidationException extends RuntimeException {

    // List of specific validation error messages
    private final List<String> errors;

    /**
     * Creates a new TradeValidationException with a message and list of errors
     * @param message general description of the validation failure
     * @param errors  list of specific validation error messages
     */
    public TradeValidationException(String message, List<String> errors) {
        super(message); // Passes the main message to RuntimeException
        this.errors = errors;
    }

    /**
     * Returns the list of validation error messages
     * @return list of error messages
     */
    public List<String> getErrors() {
        return errors;
    }
}