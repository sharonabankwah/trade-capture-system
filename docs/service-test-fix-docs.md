# Trade Service Tests Documentation

## Overview
This document summarises the key changes and fixes made to the Trade Service unit tests. Each section outlines the problem, root cause, solution, and impact on the tests.

---

## 1. `testCreateTrade_Success` Fix

**Commit:** `fix(test): made testCreateTrade_Success pass by mocking required dependencies`

### Problem
The `testCreateTrade_Success` was failing due to multiple `NullPointerExceptions`. The service method `createTrade` depends on reference data (Book, Counterparty, TradeStatus) and saved TradeLegs, which were not properly mocked in the test.

### Root Cause
Mocks for `BookRepository`, `CounterpartyRepository`, `TradeStatusRepository`, and `TradeLegRepository` were missing. As a result, calls like:
- `bookRepository.findByBookName()`
- `counterpartyRepository.findByName()`
- `tradeStatusRepository.findByTradeStatus()`
- `tradeLegRepository.save()`

returned `null`, causing validation and cashflow generation to fail.

### Solution
- Added required values to the `TradeDTO` (bookName, counterpartyName, tradeStatus).  
- Created mock entities for Book, Counterparty, TradeStatus, and TradeLeg.  
- Configured repository mocks to return these entities.

### Impact
The test now passes, exercising `createTrade` fully with proper reference data and trade leg creation.

---

## 2. `testCreateTrade_InvalidDates_ShouldFail` Fix

**Commit:** `fix(test): correct expected error message in testCreateTrade_InvalidDates_ShouldFail`

### Problem
The test was failing because the expected exception message did not match the actual message thrown by the `TradeService` validation logic.

### Root Cause
The test originally contained a placeholder message ("Wrong error message"). The service correctly throws:  
`"Start date cannot be before trade date"` when the start date is before the trade date.

### Solution
Updated the test assertion to expect the correct message, aligning it with the service’s behavior.

### Impact
The test now correctly verifies trade date validation logic and passes successfully.

---

## 3. `testAmendTrade_Success` Fix

**Commit:** `fix(test): fixed NullPointerException in testAmendTrade_Success`

### Problem
`testAmendTrade_Success` failed due to a `NullPointerException` when `generateCashflows()` was called. The method attempted to access `leg.getLegId()`, which returned `null`.

### Root Cause
The `tradeLegRepository.save()` call returned `null` because the repository mock was not configured to return a valid `TradeLeg`.

### Solution
- Added a mocked `TradeLeg` object with a `legId`.  
- Configured `tradeLegRepository.save()` to return the mocked leg.  

### Impact
The test now passes, confirming that trade amendments correctly save and handle trade legs.

---

## 4. `testCashflowGeneration_MonthlySchedule` Implementation

**Commit:** `fix(test): implement testCashflowGeneration_MonthlySchedule to test for monthly cashflow generation`

### Problem
The original test was invalid due to logical errors, missing TradeLeg setup, and unmocked repositories.

### Root Cause
- `TradeDTO` and `TradeLegDTO` were not fully populated.  
- Service methods relied on repository mocks, which were missing, leading to runtime errors.

### Solution
- Populated `TradeDTO` with book name, counterparty, trade status, start and maturity dates.  
- Added two `TradeLegDTOs` with notional and rate values.  
- Created corresponding `TradeLeg` entities with `legId`s to mock repository saves.  
- Mocked `bookRepository`, `counterpartyRepository`, `tradeStatusRepository`, `tradeLegRepository`, `tradeRepository`, and `cashflowRepository`.  
- Verified cashflows were generated and saved.  
- Added assertions to ensure results are not null and contain expected trade legs.

### Impact
- The test now fully verifies monthly cashflow generation.  
- Ensures trade legs and cashflows are handled correctly.  

---

## 5. Schedule Setup for Monthly Cashflow Test

**Commit:** `test(update): included Schedule setup for monthly cashflow generation test`

### Details
- Added a `Schedule` object with a 1M time frame.  
- Linked it to both trade legs in `testCashflowGeneration_MonthlySchedule`.  

### Impact
- Ensures the test accurately reflects a monthly schedule.  
- Improves realism of cashflow generation logic.

---
