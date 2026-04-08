package com.finance.tracker.controller;

import com.finance.tracker.dto.TransactionRequest;
import com.finance.tracker.dto.TransactionResponse;
import com.finance.tracker.dto.TransactionUpdateRequest;
import com.finance.tracker.exception.CommonServiceException;
import com.finance.tracker.service.TransactionService;
import com.finance.tracker.exception.TransactionException;
import com.finance.tracker.util.BaseResponse;
import com.finance.tracker.util.enums.Category;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<BaseResponse<TransactionResponse>> addTransaction(
            @Valid @RequestBody TransactionRequest transactionRequest) throws CommonServiceException {
        log.debug("Processing transaction creation request");
        try {

            TransactionResponse transactionResponse = transactionService.addTransaction(transactionRequest);

            return ResponseEntity.ok(
                    BaseResponse.<TransactionResponse>builder()
                            .success(true)
                            .timestamp(new Date().getTime())
                            .message("Transaction successfully created")
                            .payload(transactionResponse)
                            .build());
        } catch (Exception e) {
            log.error("Failed to add transaction");
            throw e;
        }
    }

    @PatchMapping
    public ResponseEntity<BaseResponse<TransactionResponse>> updateTransaction(
            @Valid @RequestBody TransactionUpdateRequest transactionUpdateRequest) throws TransactionException, CommonServiceException {
        log.debug("Processing transaction update request");
        try {

            return ResponseEntity.ok(
                    BaseResponse.<TransactionResponse>builder()
                            .success(true)
                            .timestamp(new Date().getTime())
                            .message("Transaction successfully updated")
                            .payload(transactionService.updateTransaction(transactionUpdateRequest))
                            .build());
        } catch (Exception e) {
            log.error("Failed to update transaction");
            throw e;
        }
    }
    @DeleteMapping("/{transactionId}")
    public ResponseEntity<BaseResponse<String>> deleteTransaction(@PathVariable Long transactionId) throws TransactionException, CommonServiceException {
        transactionService.deleteTransaction(transactionId);
        return ResponseEntity.ok(
                BaseResponse.<String>builder()
                        .success(true)
                        .timestamp(new Date().getTime())
                        .message("Transaction Deleted successfully")
                        .build());
    }

    @GetMapping("/filter")
    public ResponseEntity<List<TransactionResponse>> getFilteredTransactions(
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(required = false) Category category) throws CommonServiceException {

        List<TransactionResponse> transactionResponseList = transactionService.getTransactionsByFilter(fromDate, toDate, category);

        return ResponseEntity.ok(
                transactionResponseList
        );
    }

}
