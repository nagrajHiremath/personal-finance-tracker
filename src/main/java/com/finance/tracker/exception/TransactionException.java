package com.finance.tracker.exception;

import com.finance.tracker.entity.UserEntity;
import com.finance.tracker.util.enums.Category;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TransactionException extends Exception {

    private Long transactionId;

    private Double amount;
    private LocalDate date;
    private String description;

    @Enumerated(EnumType.STRING)
    private Category category;

    @ManyToOne
    private UserEntity user;

    public TransactionException(String message, Long transactionId, Double amount, LocalDate date, String description) {
        super(message);
        this.transactionId = transactionId;
        this.amount = amount;
        this.date = date;
        this.description = description;
    }
}
