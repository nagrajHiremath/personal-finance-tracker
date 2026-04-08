package com.finance.tracker.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.finance.tracker.util.enums.Category;
import lombok.Data;
import lombok.NonNull;

import java.time.LocalDate;
@Data
public class TransactionUpdateRequest {
    @NonNull
    private Long transactionId;

    private Double amount;
    private String description;

    private Category category;

}
