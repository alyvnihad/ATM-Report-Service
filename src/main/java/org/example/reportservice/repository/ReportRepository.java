package org.example.reportservice.repository;

import org.example.reportservice.model.CustomerReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<CustomerReport,Long> {
    Optional<CustomerReport> findByAccountIdAndType(Long accountId, String type);
    List<CustomerReport> findTop10ByCreatedAtBetweenOrderByAmountDesc(LocalDateTime start, LocalDateTime end);
}
