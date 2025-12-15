package org.example.reportservice.payload;

import lombok.Data;

@Data
public class CustomerReportPayload {
    private Long accountId;
    private Long cardNumber;
    private Double amount;
    private String type;
}
