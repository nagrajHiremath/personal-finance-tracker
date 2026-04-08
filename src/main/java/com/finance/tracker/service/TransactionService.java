package com.finance.tracker.service;

import com.finance.tracker.dto.TransactionRequest;
import com.finance.tracker.dto.TransactionResponse;
import com.finance.tracker.dto.TransactionUpdateRequest;
import com.finance.tracker.entity.TransactionEntity;
import com.finance.tracker.entity.UserEntity;
import com.finance.tracker.exception.CommonServiceException;
import com.finance.tracker.repository.TransactionRepository;
import com.finance.tracker.exception.TransactionException;
import com.finance.tracker.repository.UserRepository;
import com.finance.tracker.util.enums.Category;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

public TransactionResponse addTransaction(TransactionRequest transactionRequest)
        throws CommonServiceException {

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String email = auth.getName();

    UserEntity user = userRepository.findByEmail(email)
            .orElseThrow(() -> new CommonServiceException(
                    "User not found",
                    HttpStatus.NOT_FOUND,
                    0x1771));

    TransactionEntity transactionEntity =
            modelMapper.map(transactionRequest, TransactionEntity.class);

    transactionEntity.setDate(LocalDate.now());
    transactionEntity.setUser(user);

    TransactionEntity savedEntity = transactionRepository.save(transactionEntity);

    return modelMapper.map(savedEntity, TransactionResponse.class);
}


    public TransactionResponse updateTransaction(TransactionUpdateRequest transactionUpdateRequest)
            throws CommonServiceException {

        TransactionEntity existingTransaction = transactionRepository
                .findById(transactionUpdateRequest.getTransactionId())
                .orElseThrow(() -> new CommonServiceException(
                        "Transaction not found with id: "+ transactionUpdateRequest.getTransactionId(),
                        HttpStatus.NOT_FOUND,
                        0x1771));

        modelMapper.map(transactionUpdateRequest, existingTransaction);

        TransactionEntity saved = transactionRepository.save(existingTransaction);

        return modelMapper.map(saved, TransactionResponse.class);
    }


    public void deleteTransaction(Long transactionId) throws CommonServiceException {

        TransactionEntity transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new CommonServiceException(
                        "Transaction not found with id: "+transactionId,
                        HttpStatus.NOT_FOUND,
                        0x1771));

        transactionRepository.delete(transaction);
    }


    public List<TransactionResponse> getTransactionsByFilter(
            LocalDate fromDate,
            LocalDate toDate,
            Category category) throws CommonServiceException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CommonServiceException(
                        "User not found",
                        HttpStatus.NOT_FOUND,
                        0x1771));

        Long userId = user.getUserId();

        Specification<TransactionEntity> spec = (root, query, cb) ->
                cb.equal(root.get("user").get("userId"), userId); // filter by user

        if (fromDate != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("date"), fromDate));
        }

        if (toDate != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("date"), toDate));
        }

        if (category != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("category"), category));
        }

        List<TransactionEntity> transactions = transactionRepository.findAll(spec);

        return transactions.stream()
                .map(t -> modelMapper.map(t, TransactionResponse.class))
                .toList();
    }

}
