package com.finance.tracker.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.finance.tracker.util.enums.Category;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TransactionRequest {

    @NotNull
    private Double amount;

    private String description;

    @NotNull
    private Category category;
}
