package org.example.reportservice.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TransactionResponse {
    private Long id;
    private Long accountId;
    private Long atmId;
    private Long cardNumber;
    private Double amount;
    private String type;
    private LocalDateTime createdAt;
}
