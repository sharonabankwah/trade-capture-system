### Book Service Tests Documentation

### fix(test): updated testFindBookById() to mock BookMapper and map Book to BookDTO

**Problem:**  
The `testFindBookById()` was failing with a `NullPointerException` because the `BookDTO` was never being returned.

**Root Cause:**  
The book entity retrieved from the repository wasn’t mapped to a `BookDTO` as per the service logic, so the test received a null value.

**Solution:**  
Mocked a `BookDTO` object and set its ID, then configured the `BookMapper` mock to return the `BookDTO` when mapping the `Book` entity. This ensures the mapping behaviour in the service is correctly simulated.

**Impact:**  
The test now passes successfully, verifying that the `BookDTO` is present and that the IDs of the `Book` and `BookDTO` match as expected.

---

### fix(test): updated testSaveBook() to mock BookMapper, converting between Book and BookDTO

**Problem:**  
The `testSaveBook()` was failing with a `NullPointerException` because the service method tried to call the `BookMapper` to map between `Book` and `BookDTO`, but the mapper wasn’t mocked in the test.

**Root Cause:**  
The calls `bookMapper.toEntity(bookDTO)` and `bookMapper.toDTO(book)` returned null since the mapper was not invoked in the test.

**Solution:**  
Mocked `bookMapper.toEntity()` to return a `Book` when given a `BookDTO` and `bookMapper.toDTO()` to return a `BookDTO` when given a `Book`.

**Impact:**  
The test now passes successfully, verifying that the service returns a non-null `BookDTO` and that the returned `BookDTO` has the same ID as expected in the test.
