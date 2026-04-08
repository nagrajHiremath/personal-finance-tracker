package com.finance.tracker.repository;

import com.finance.tracker.entity.TransactionEntity;
import com.finance.tracker.util.enums.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, Long>, JpaSpecificationExecutor<TransactionEntity> {

    List<TransactionEntity> findByDateBetweenAndCategory(
            LocalDate fromDate,
            LocalDate toDate,
            Category category
    );

    List<TransactionEntity> findByDateBetween(
            LocalDate fromDate,
            LocalDate toDate
    );

}
