package org.example.reportservice.repository;

import org.example.reportservice.model.CustomerReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReportRepository extends JpaRepository<CustomerReport,Long> {
    Optional<CustomerReport> findByAccountIdAndType(Long accountId, String type);
}
