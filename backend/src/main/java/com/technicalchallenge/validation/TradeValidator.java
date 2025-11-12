package com.technicalchallenge.validation;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

import com.technicalchallenge.dto.TradeDTO;
import com.technicalchallenge.dto.TradeLegDTO;
import com.technicalchallenge.model.Book;
import com.technicalchallenge.model.Counterparty;
import com.technicalchallenge.model.ApplicationUser;
import com.technicalchallenge.repository.ApplicationUserRepository;
import com.technicalchallenge.repository.BookRepository;
import com.technicalchallenge.repository.CounterpartyRepository;

/**
 * Service responsible for validating all business rules related to trades
 * Includes checks for trade dates, entity statuses, and trade leg consistency
 */

@Service
public class TradeValidator {

    private static final Logger logger = LoggerFactory.getLogger(TradeValidator.class);

    private final BookRepository bookRepository;
    private final CounterpartyRepository counterpartyRepository;
    private final ApplicationUserRepository applicationUserRepository;

    public TradeValidator(BookRepository bookRepository, 
                          CounterpartyRepository counterpartyRepository, 
                          ApplicationUserRepository applicationUserRepository) {
        this.bookRepository = bookRepository;
        this.counterpartyRepository = counterpartyRepository;
        this.applicationUserRepository = applicationUserRepository;
    }

    /**
     * Validates business rules for a trade
     * Includes checks for trade dates, related entity activity and leg consistency
     * @param tradeDTO Trade data to  validate
     * @return ValidationResult containing any errors found
     */
    public ValidationResult validateTradeBusinessRules(TradeDTO tradeDTO) {
        
        ValidationResult validationResult = new ValidationResult();

        logger.info("Starting trade validation for tradeId: {}", tradeDTO.getTradeId());

        // Validate trade-level rules
        validateTradeDates(tradeDTO, validationResult);
        if (validationResult.hasErrors()) {
            logger.warn("Date validation failed for tradeId {}: {}", tradeDTO.getTradeId(), validationResult.getErrors());
        }
        // Validate book, counterparty and trade entities
        validateEntityStatus(tradeDTO, validationResult);
        if (validationResult.hasErrors()) {
            logger.warn("Entity validation failed for tradeId {}: {}", tradeDTO.getTradeId(), validationResult.getErrors());
        }

        // Validate leg-level rules (if legs are present)
        if (tradeDTO.getTradeLegs() != null && !tradeDTO.getTradeLegs().isEmpty()) {
            ValidationResult legResult = validateTradeLegConsistency(tradeDTO.getTradeLegs());
            validationResult.getErrors().addAll(legResult.getErrors());

            if (legResult.hasErrors()) {
                logger.warn("Leg validation failed for tradeId {}: {}", tradeDTO.getTradeId(), legResult.getErrors());
            }
        } else {
            validationResult.addError("Trade must contain exactly two legs");
            logger.warn("Trade validation failed for tradeId {}: missing or invalid legs", tradeDTO.getTradeId());
        }

        if (validationResult.hasErrors()) {
            logger.info("Trade validation completed with {} error(s) for tradeId {}.", validationResult.getErrors().size(), tradeDTO.getTradeId());
        } else {
            logger.info("Trade validation passed successfully for tradeId {}.", tradeDTO.getTradeId());
        }

        return validationResult;
    }

     /**
     * Validates cross-leg rules such as pay/receive flags and leg type consistency
     * @param legs List of TradeLegDTO objects representing trade legs
     * @return ValidationResult containing any leg-specific errors
     */
    public ValidationResult validateTradeLegConsistency(List<TradeLegDTO> legs) {
        
        ValidationResult validationResult = new ValidationResult();

        if (legs == null || legs.size() != 2) {
            validationResult.addError("Trade must contain exactly two legs");

            return validationResult;
        }

        TradeLegDTO leg1 = legs.get(0);
        TradeLegDTO leg2 = legs.get(1);

        // Validates pay/receive flags
        if (leg1.getPayReceiveFlag() == null || leg2.getPayReceiveFlag() == null) {
            validationResult.addError("Each leg must specify a pay/receive flag");
        } else if (leg1.getPayReceiveFlag().equalsIgnoreCase(leg2.getPayReceiveFlag())) {
            validationResult.addError("Trade legs must have opposite pay/receive flags");
        }

        // Validates each leg's details
        validateLegType(leg1, validationResult);
        validateLegType(leg2, validationResult);

        return validationResult;
    }

    /**
     * Validates logical relationships between trade-related dates
     * @param tradeDTO Trade data
     * @param result   ValidationResult to store errors
     */
    private void validateTradeDates(TradeDTO tradeDTO, ValidationResult result) {
        LocalDate tradeDate = tradeDTO.getTradeDate();
        LocalDate startDate = tradeDTO.getTradeStartDate();
        LocalDate maturityDate = tradeDTO.getTradeMaturityDate();

        // Checks for missing dates
        if (tradeDate == null) result.addError("Trade date is required");
        if (startDate == null) result.addError("Start date is required");
        if (maturityDate == null) result.addError("Maturity date is required");

        // Only check relationships if all dates are present
        if (tradeDate != null && startDate != null && maturityDate != null) {
            if (maturityDate.isBefore(startDate)) result.addError("Maturity date cannot be before start date");
            if (startDate.isBefore(tradeDate)) result.addError("Start date cannot be before trade date");
            if (tradeDate.isBefore(LocalDate.now().minusDays(30))) result
              .addError("Trade date cannot be more than 30 days in the past");
        }
    }

    /**
     * Validates that all referenced entities (Book, Counterparty, Trader) exist and are active
     * @param tradeDTO Trade data
     * @param result   ValidationResult to store errors
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

    /**
     * Validates individual leg type rules for Fixed or Floating legs
     * @param leg Trade leg to validate
     * @param result ValidationResult to store errors
     */
    private void validateLegType(TradeLegDTO leg, ValidationResult result) {

        if (leg.getLegType() == null) {
            result.addError("Each leg must have a legType (e.g., Fixed or Floating)");

            return;
        }

        String legType = leg.getLegType().toUpperCase();

        switch (legType) {
            case "FLOATING" -> {
                if (leg.getIndexName() == null || leg.getIndexName().isBlank()) {
                    result.addError("Floating leg must specify an index");
                }
            }
            case "FIXED" -> {
                if (leg.getRate() == null || leg.getRate() <= 0) {
                    result.addError("Fixed leg must have a valid positive rate");
                }
            }
            default -> result.addError("Invalid legType: " + legType + ". Must be FIXED or FLOATING");
        }
    }
}
