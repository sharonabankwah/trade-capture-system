package com.technicalchallenge.specification;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import com.technicalchallenge.model.Trade;

import java.time.LocalDate;

@Component
public class TradeSpecificationBuilder {

    public Specification<Trade> buildTradeSpecification(
            String counterpartyName,
            String bookName,
            Long traderUserId,
            String tradeStatus,
            LocalDate tradeDate,
            LocalDate tradeStartDate,
            LocalDate tradeMaturityDate) {

        Specification<Trade> spec = Specification.where(null);

        if (counterpartyName != null && !counterpartyName.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("counterparty").get("name")), 
                    "%" + counterpartyName.toLowerCase() + "%"));
        }

        if (bookName != null && !bookName.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("book").get("bookName")), 
                    "%" + bookName.toLowerCase() + "%"));
        }

        if (traderUserId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("traderUser").get("id"),
            traderUserId));
        }

        if (tradeStatus != null && !tradeStatus.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("tradeStatus").get("tradeStatus"),
            tradeStatus));
        }

        if (tradeDate != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("tradeDate"), tradeDate));
        }

        if (tradeStartDate != null && tradeMaturityDate != null) {
            spec = spec.and((root, query, cb) ->
                    cb.between(root.get("tradeStartDate"), tradeStartDate, tradeMaturityDate));
        } else if (tradeStartDate != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("tradeStartDate"), tradeStartDate));
        } else if (tradeMaturityDate != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("tradeMaturityDate"), tradeMaturityDate));
        }

        return spec;
    }
}