package org.example.reportservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.reportservice.dto.ReportRequest;
import org.example.reportservice.dto.TransactionRequest;
import org.example.reportservice.payload.CustomerReportPayload;
import org.example.reportservice.service.ReportService;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/pdf")
    public String pdfGenerated(@RequestBody ReportRequest payload){
        return reportService.pdfGenerated(payload);
    }

    @GetMapping("/daily-log/{date}")
    public String dailyLog(@PathVariable LocalDate date, @RequestBody TransactionRequest request) throws IOException {
        return reportService.dailyLog(date,request);
    }

    @PostMapping("/customer-report")
    public void customerReport(@RequestBody CustomerReportPayload payload){
        reportService.customerReport(payload);
    }
}
