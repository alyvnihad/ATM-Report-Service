package org.example.reportservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.reportservice.dto.ReportRequest;
import org.example.reportservice.service.ReportService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/pdf")
    public String pdfGenerated(@RequestBody ReportRequest payload){
        return reportService.pdfGenerated(payload);
    }
}
