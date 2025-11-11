package com.technicalchallenge.validation;

import java.time.LocalDate;
import java.util.List;

import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import com.technicalchallenge.dto.TradeDTO;
import com.technicalchallenge.dto.TradeLegDTO;
import com.technicalchallenge.model.Book;
import com.technicalchallenge.model.Counterparty;
import com.technicalchallenge.model.ApplicationUser;
import com.technicalchallenge.repository.ApplicationUserRepository;
import com.technicalchallenge.repository.BookRepository;
import com.technicalchallenge.repository.CounterpartyRepository;

@Service
public class TradeValidationService {

    private final BookRepository bookRepository;
    private final CounterpartyRepository counterpartyRepository;
    private final ApplicationUserRepository applicationUserRepository;

    public TradeValidationService(BookRepository bookRepository, CounterpartyRepository counterpartyRepository, ApplicationUserRepository applicationUserRepository) {
        this.bookRepository = bookRepository;
        this.counterpartyRepository = counterpartyRepository;
        this.applicationUserRepository = applicationUserRepository;
    }

    /**
     * Validate business rules for a trade
     */
    public ValidationResult validateTradeBusinessRules(TradeDTO tradeDTO) {
        
        ValidationResult result = new ValidationResult();

        validateTradeDates(tradeDTO, result);
        validateEntityStatus(tradeDTO, result);

        return result;
    }

    /**
     * Validate trade-related dates
     */
    private void validateTradeDates(TradeDTO tradeDTO, ValidationResult result) {
        LocalDate tradeDate = tradeDTO.getTradeDate();
        LocalDate startDate = tradeDTO.getTradeStartDate();
        LocalDate maturityDate = tradeDTO.getTradeMaturityDate();

        // Checks for missing dates
        if (tradeDate == null) result.addError("Trade date is required");
        if (startDate == null) result.addError("Start date is required");
        if (maturityDate == null) result.addError("Maturity date is required");

        // Only checks relationships if all dates are present
        if (tradeDate != null && startDate != null && maturityDate != null) {
            if (maturityDate.isBefore(startDate)) result.addError("Maturity date cannot be before start date");
            if (startDate.isBefore(tradeDate)) result.addError("Start date cannot be before trade date");
            if (tradeDate.isBefore(LocalDate.now().minusDays(30))) result
              .addError("Trade date cannot be more than 30 days in the past");
        }
    }

    /**
     * Validate the status of related entities: Book, Counterparty and Trader
     */
    private void validateEntityStatus(TradeDTO tradeDTO, ValidationResult result) {

        // Book validation
        bookRepository.findById(tradeDTO.getBookId())
            .filter(Book::isActive)
            .orElseGet(() -> {
                result.addError("Book does not exist or is inactive");
                return null;
            });

        // Counterparty validation
        counterpartyRepository.findById(tradeDTO.getCounterpartyId())
            .filter(Counterparty::isActive)
            .orElseGet(() -> {
                result.addError("Counterparty does not exist or is inactive");
                return null;
            });

        // Trader validation
        applicationUserRepository.findById(tradeDTO.getTraderUserId())
            .filter(ApplicationUser::isActive)
            .orElseGet(() -> {
                result.addError("Trader user not found or inactive");
                return null;
            });
    }
}
//   public boolean validateUserPrivileges(String userId, String operation, TradeDTO tradeDTO) {}
//   public ValidationResult validateTradeLegConsistency(List<TradeLegDTO> legs) {}
