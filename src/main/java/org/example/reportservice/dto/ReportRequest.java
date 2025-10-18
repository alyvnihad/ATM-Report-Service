package org.example.reportservice.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ReportRequest {
    private Long accountNumber;
    private String name;
    private String email;
    private Long cardNumber;
    private LocalDate expiryDate;
    private String currency;
    private String paymentNetwork;
}
