package com.finance.tracker.entity;

import com.finance.tracker.util.enums.Category;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Data
@Builder
@Table(name = "Transaction")
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long transactionId;
    @Column(name = "amount")
    private Double amount;
    @Column(name = "date")
    private LocalDate date;
    @Column(name = "description")
    private String description;
    @Column(name = "category")
    @Enumerated(EnumType.STRING)
    private Category category;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;
}
