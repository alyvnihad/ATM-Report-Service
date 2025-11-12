package org.example.reportservice.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TransactionRequest {
    private LocalDate date;
    private String refreshToken;
}
