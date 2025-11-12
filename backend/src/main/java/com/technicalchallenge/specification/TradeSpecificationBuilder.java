package com.technicalchallenge.specification;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import com.technicalchallenge.model.Trade;

import java.time.LocalDate;

/**
 * Builds dynamic JPA Specification objects for filtering Trade entities
 * This class constructs query specifications based on various optional filtering
 * parameters such as counterparty name, book name, trader user ID, trade status,
 * and date ranges. The resulting Specification<Trade> can be used with
 * Spring Data JPA repositories to execute dynamic, criteria-based queries
 */
@Component
public class TradeSpecificationBuilder {

    /**
     * Builds a Specification for Trade entities based on the provided filter parameters
     * Any parameter that is {@code null} or blank (for strings) will be ignored
     * The resulting specification will combine applicable filters using logical AND
     * @param counterpartyName   optional name of the counterparty to filter trades by (case-insensitive, partial match)
     * @param bookName           optional book name to filter trades by (case-insensitive, partial match)
     * @param traderUserId       optional trader user ID to filter trades by
     * @param tradeStatus        optional trade status to filter trades by (exact match)
     * @param tradeDate          optional trade date to filter trades by (exact match)
     * @param tradeStartDate     optional lower bound for trade start date range
     * @param tradeMaturityDate  optional upper bound for trade maturity date range
     * @return a Specification representing all applied filters
     */
    public Specification<Trade> buildTradeSpecification(
            String counterpartyName,
            String bookName,
            Long traderUserId,
            String tradeStatus,
            LocalDate tradeDate,
            LocalDate tradeStartDate,
            LocalDate tradeMaturityDate) {

        // Starts with an empty Specification that we can chain conditions onto
        Specification<Trade> spec = Specification.where(null);

        // Filter by counterparty name
        if (counterpartyName != null && !counterpartyName.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("counterparty").get("name")), 
                    "%" + counterpartyName.toLowerCase() + "%"));
        }
        // Filter by book name
        if (bookName != null && !bookName.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("book").get("bookName")), 
                    "%" + bookName.toLowerCase() + "%"));
        }
        // Filter by trader user ID
        if (traderUserId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("traderUser").get("id"),
            traderUserId));
        }
        // Filter by trade status
        if (tradeStatus != null && !tradeStatus.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("tradeStatus").get("tradeStatus"),
            tradeStatus));
        }
        // Filter by trade date
        if (tradeDate != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("tradeDate"), tradeDate));
        }
        // Filter by trade start and/or maturity date range
        if (tradeStartDate != null && tradeMaturityDate != null) {
            // Both start and maturity dates are provided → use BETWEEN
            spec = spec.and((root, query, cb) ->
                    cb.between(root.get("tradeStartDate"), tradeStartDate, tradeMaturityDate));
        } else if (tradeStartDate != null) {
            // Only start date provided → greater than or equal to start date
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("tradeStartDate"), tradeStartDate));
        } else if (tradeMaturityDate != null) {
            // Only maturity date provided → less than or equal to maturity date
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("tradeMaturityDate"), tradeMaturityDate));
        }
        
        // Returns the fully constructed Specification
        return spec;
    }
}