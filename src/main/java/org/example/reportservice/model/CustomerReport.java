package org.example.reportservice.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"account_id", "type"})})
@Data
public class CustomerReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long accountId;
    private Long cardNumber;
    private Double amount;
    private LocalDateTime createdAt;
    private String type;
}
