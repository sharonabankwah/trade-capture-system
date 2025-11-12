### Implement Missing Functionality

### Enhancement 1: Multi-Criteria Search, Paginated Filtering, RSQL Query Support

### **Implementation Summary**

- Implemented three endpoints in `TradeController`:
    1. **`/search`**: Allows multi-criteria search using optional parameters such as counterparty name, book name, trader ID, trade status, and trade dates.
    2. **`/filter`**: Adds pagination and sorting support, enabling users to filter trades page by page with custom sort options.
    3. **`/rsql`**: Provides advanced filtering for power users by converting an RSQL query string into a JPA Specification, supporting complex, dynamic queries.
- Key service methods:
    - `getTradeByMultiCriteriaSearch()`
    - `getTradesWithFiltersAndPagination()`
    - `getTradesByRsql()`

### **Reasoning for Choices**

- Used **CriteriaBuilder** for `/search` because it allows dynamic, optional filtering based on which query parameters are provided.
- Used **Spring Data `Specification` + `Pageable`** for `/filter` to integrate pagination and sorting cleanly with the repository layer.
- Used **RSQL + `RSQLJPASupport.toSpecification()`** for `/rsql` to support advanced query parsing, providing power users the flexibility to perform ad-hoc filtering.

### **Issues Encountered**

- Ensuring optional parameters don’t break the query required careful handling of null checks and `Predicate` combinations.
- Mapping RSQL queries to JPA Specifications initially caused `CriteriaBuilder` type mismatches; resolved by checking the RSQL parser version and supported types.
- Deciding which fields should support partial vs exact matches  - settled on exact matches for reference data fields and range matching for dates.

### **Enhancements / Improvements**

- Could add **case-insensitive search** for names to improve usability.
- Could add **default sorting** when `sortBy` is not specified to improve API consistency.
- For very large datasets, consider **caching frequent queries** or implementing **search indexes**.

### **Lessons Learned**

- Dynamic query construction in JPA requires careful handling of optional fields and null predicates.
- Combining Spring Data pagination with Specifications is very powerful for REST APIs.
- RSQL integration is useful for advanced filtering but needs careful documentation for end users.

---

## Enhancement 2: Trade Business Rule Validation and User Privileges

### **Implementation Summary**

- Implemented **`TradeValidator`** service:
    - `validateTradeBusinessRules(TradeDTO tradeDTO)` → validates trade date rules, entity status (book, counterparty, trader), and other general trade rules.
    - `validateTradeLegConsistency(List<TradeLegDTO> legs)` → validates cross-leg rules (opposite pay/receive flags, fixed vs floating leg requirements).
- Implemented **`UserPrivilegeValidator`** service:
    - `validateUserPrivileges(String userId, String operation, TradeDTO tradeDTO)` → enforces role-based access control for CREATE, AMEND, TERMINATE, VIEW operations.
- Created custom exceptions:
    - `TradeValidationException` → thrown when business rules fail.
    - `UserPrivilegeValidationException` → thrown when a user lacks required privileges.

### **Reasoning for Choices**

- Chose **service-based validation** (instead of directly in controller) for separation of concerns and reusability.
- Used **role-based switch logic** in `UserPrivilegeValidator` to clearly define what each role can/cannot do.
- Validated **trade legs separately** to encapsulate complex business rules (pay/receive, rate/index) in a dedicated helper method.

### **Issues Encountered**

- Handling nullable fields in `TradeLegDTO` and `TradeDTO` required careful null checks to avoid runtime errors.
- Decided whether to validate privileges based on `userId` or `userName`; eventually chose `loginId` with case-insensitive matching.
- Initial confusion on `tradeId` vs entity `id` caused multiple “Trade not found” errors; resolved by understanding how the DTO maps to the entity.

### **Enhancements / Improvements**

- Could enhance leg validation to support more than two legs for future multi-leg trades.
- Could implement **centralised error reporting** with a consistent error format for API consumers.
- Could extend privilege validation to include hierarchical or delegated roles in the future.

### **Lessons Learned**

- Clear separation of **validation vs persistence logic** reduces risk of breaking core trade operations.
- Null safety and careful use of DTOs vs entities is critical in complex business applications.
- Role-based access control can be cleanly implemented with switch/case logic and proper case-insensitive matching.
- Writing detailed **unit tests for each validator** ensures robustness without affecting the main trade workflow.

### **Current Status – Trade Creation Validation Issue (Amendment)**

**Overview:**
After implementing the user privilege validation logic, all trade creation attempts are failing due to privilege checks, regardless of the user. This includes TRADER_SALES, MIDDLE_OFFICE, SUPPORT, and other roles.

**Observed Behaviour:**

- Every attempt to create a trade triggers a UserPrivilegeValidationException.
- Logs confirm the validation check is executing correctly but always evaluates to false for ownership or permissions.
- Payloads and test data changes (e.g., modifying traderUserId, traderUserName, inputterUserName) do not resolve the failure.

**Likely Cause:**

- The underlying repository/service that the validator uses (userPrivilegeRepository or equivalent) seems to expect a domain-specific ID or format that is not provided by the current payload.
- This indicates a known bug in the privilege validation integration rather than an issue with individual users or the validator logic itself.

**Impact:**

- Trade creation functionality is blocked for all users.
- Existing roles (e.g., MO, SUPPORT) cannot create trades, even if the validator logic theoretically allows it.

**Next Steps:**

- Investigate and fix the repository/service mapping so that privilege checks correctly reflect user permissions.

**Note:**

- This issue surfaced immediately after adding the new validation logic. No other changes to the system appear to be contributing.
