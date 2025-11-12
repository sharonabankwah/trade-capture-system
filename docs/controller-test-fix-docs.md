### Trade Controller Tests Documentation

### Overview
This document summarises the key changes and fixes made to the Trade Controller unit tests. Each section outlines the problem, root cause, solution, and impact on the tests.

---

### fix(test): adjust testCreateTrade to return 200 instead of 201

**Problem:**  
The test expected an HTTP 200 response (`.andExpect(status().isOk)`) but received 201 instead.

**Root Cause:**  
The `createTrade()` method returned `ResponseEntity.status(HttpStatus.CREATED).body(responseDTO)`, resulting in a 201 status code rather than the expected 200.

**Solution:**  
Updated the `createTrade()` method to return `ResponseEntity.ok(responseDTO)` so that it now returns a 200 status code.

**Impact:**  
The test now passes successfully, with the actual and expected HTTP statuses matching (200 OK).

---

### fix(test): adjust testCreateTradeValidationFailure_MissingBook to return 400 instead of 200

**Problem:**  
The test expected an HTTP 400 response (`.andExpect(status().isBadRequest())`) but received 200 instead.

**Root Cause:**  
The `createTrade()` method did not validate missing book or counterparty information, so it never triggered an error when a trade was created without these details.

**Solution:**  
Added validation in `createTrade()` to check if the `tradeDTO` has an empty book name or counterparty name. If either is missing, the method now returns an HTTP 400 response.

**Impact:**  
The test now passes, and `createTrade()` correctly handles cases where book or counterparty data is missing.

---

### fix(test): adjust testDeleteTrade to return 204 instead of 200

**Problem:**  
The test expected an HTTP 204 response (`.andExpect(status().isNoContent())`) but received 200 instead.

**Root Cause:**  
The `deleteTrade()` method returned an HTTP 200 response with a body confirming deletion, rather than the expected 204 No Content.

**Solution:**  
Updated `deleteTrade()` to return `ResponseEntity.noContent().build()`, ensuring deletion succeeds without returning a response body.

**Impact:**  
The test now passes successfully, and `deleteTrade()` correctly returns 204 when a trade is deleted.

---

### fix(test): removed manual validation checks from createTrade() method in the controller

**Problem:**  
`createTrade()` relied on manual if-statements to validate book name, counterparty name, and trade date, which were redundant.

**Root Cause:**  
Spring’s `@Valid` annotation triggered validation before `createTrade()` was executed, so the manual checks were never reached when invalid data was submitted.

**Solution:**  
Removed the redundant if-statements from the controller and added a `GlobalExceptionHandler` to catch validation failures and return custom error messages.

**Impact:**  
Controller logic is simplified, and validation handling is centralised, improving maintainability.

---

### fix(test): added @NotNull validation message for bookName in TradeDTO

**Problem:**  
`testCreateTradeValidationFailure_MissingBook()` expected the message “Book and Counterparty are required” but did not receive it.

**Root Cause:**  
`bookName` in `TradeDTO` was missing a `@NotNull` annotation, so validation never triggered, and the `GlobalExceptionHandler` did not handle the exception.

**Solution:**  
Added a `@NotNull` annotation to the `bookName` field in `TradeDTO` to ensure validation triggers. The `GlobalExceptionHandler` now returns the custom message for a missing book or counterparty.

**Impact:**  
Validation errors for a missing `bookName` now return the expected message, ensuring consistency with test requirements.

---

### fix(test): add GlobalExceptionHandler for consistent trade validation messages

**Problem:**  
The tests `testCreateTradeValidationFailure_MissingBook()` and `testCreateTradeValidationFailure_MissingTradeDate()` failed because validation errors returned default Spring messages rather than expected custom messages.

**Root Cause:**  
Controller methods relied solely on `@Valid` annotations, with no centralised exception handling to format validation errors.

**Solution:**  
Added a `GlobalExceptionHandler` class with `@ControllerAdvice` that catches `MethodArgumentNotValidException` and returns custom messages, including “Book and Counterparty are required.”

**Impact:**  
Validation errors now consistently return expected messages, ensuring tests pass and improving clarity of error responses.

---

### fix(test): update updateTrade() logic to ensure testUpdateTradeIdMismatch() returns 400

**Problem:**  
`testUpdateTradeIdMismatch()` checks that the path ID and trade ID in the request body match, returning HTTP 400 if they don’t.

**Root Cause:**  
`updateTrade()` previously overwrote the response body’s trade ID with the path ID, so mismatched IDs still resulted in HTTP 200 and allowed data changes.

**Solution:**  
Added a check comparing the path ID and request body trade ID before processing. If they don’t match, the method returns HTTP 400 and prevents data modification.

**Impact:**  
Ensures mismatched IDs are rejected, the test passes, and data integrity is maintained during trade updates.

---

### fix(test): updated testUpdateTrade() to mock amendTrade() instead of saveTrade()

**Problem:**  
`testUpdateTrade()` was identical to `testCreateTrade`, only verifying trade creation rather than updates.

**Root Cause:**  
The test was mocking `saveTrade()` instead of `amendTrade()`, so it did not properly verify update logic.

**Solution:**  
Adjusted the test to mock `amendTrade()` using `when…thenReturn()`, ensuring it calls the correct method and returns HTTP 200 for a successful update. Amended verification to confirm `amendTrade()` is invoked.

**Impact:**  
The test now correctly verifies trade updates, improving test accuracy and functionality.
