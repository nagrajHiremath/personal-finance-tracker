package com.finance.tracker.dto;

import com.finance.tracker.entity.UserEntity;
import com.finance.tracker.util.enums.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse implements Serializable {
    private Long transactionId;

    private Double amount;
    private LocalDate date;
    private String description;

    private Category category;

    private Long userId;

}
