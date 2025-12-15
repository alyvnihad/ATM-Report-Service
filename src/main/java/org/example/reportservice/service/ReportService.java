package org.example.reportservice.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.reportservice.dto.ReportRequest;
import org.example.reportservice.dto.TransactionRequest;
import org.example.reportservice.dto.TransactionResponse;
import org.example.reportservice.model.CustomerReport;
import org.example.reportservice.payload.CustomerReportPayload;
import org.example.reportservice.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;


//Service class responsible for generating PDF reports
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final RestTemplate restTemplate;
    private final ReportRepository reportRepository;

    @Value("${transaction.url}")
    private String transactionUrl;

    @Value("${folder.path}")
    private String folderPath;

    // Generates a PDF document containing the customer's account and card information.
    @Transactional
    public String pdfGenerated(ReportRequest request) {
        try {
            String folderPath = this.folderPath;

            File file = new File(folderPath);
            if (!file.exists()) {
                file.mkdirs();
            }
            String filePath = folderPath + "\\Account-Information-" + request.getCardNumber().toString().substring(7) + "-" + request.getName().toUpperCase() + ".pdf";

            Document document = new Document();

            PdfWriter.getInstance(document, new FileOutputStream(filePath));

            document.open();
            Paragraph title = new Paragraph("\tX Bank\t", FontFactory.getFont(FontFactory.COURIER_BOLD, 26, BaseColor.DARK_GRAY));
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph("\n\nAccount Number: " + request.getAccountNumber()));
            document.add(new Paragraph("Account Name: " + request.getName()));
            document.add(new Paragraph("Account Email: " + request.getEmail()));
            document.add(new Paragraph("Card Number: " + request.getCardNumber()));
            document.add(new Paragraph("Card Expiry Date: " + request.getExpiryDate()));
            document.add(new Paragraph("Card Currency: " + request.getCurrency()));
            document.add(new Paragraph("Card Payment Network: " + request.getPaymentNetwork()));

            document.close();

            return filePath;

        } catch (DocumentException | FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    // generate Daily Log pdf
    @Transactional
    public String dailyLog(LocalDate date, TransactionRequest request) throws IOException {
        request.setDate(date);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(request.getRefreshToken());
        HttpEntity<TransactionRequest> entity = new HttpEntity<>(request, httpHeaders);

        ResponseEntity<TransactionResponse> response =
                restTemplate.postForEntity(transactionUrl + "/daily-log", entity, TransactionResponse.class);
        String folderPath = this.folderPath;
        File file = new File(folderPath);
        if (file.exists()) {
            file.mkdirs();
        }
        List<TransactionResponse> list = Collections.singletonList(response.getBody());
        if (response.getBody() != null) {

            String filePath = folderPath + "\\Daily-log sheet-" + response.getBody().getDateTime().toString() + ".xlsx";
            String[] columns = {"Account_ID", "ATM_ID", "Card Number", "Amount", "Type", "Created_at"};
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Daily Log");
            Font font = workbook.createFont();
            font.setBold(true);

            Row headerRow = sheet.createRow(0);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
            }

            int rowIndex = 1;
            for (TransactionResponse result : list) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(result.getAccountId());
                row.createCell(1).setCellValue(result.getAtmId());
                row.createCell(2).setCellValue(result.getCardNumber());
                row.createCell(3).setCellValue(result.getAmount());
                row.createCell(4).setCellValue(result.getType());
                row.createCell(5).setCellValue(result.getDateTime());
            }

            try (FileOutputStream fileOutputStream = new FileOutputStream(filePath)) {
                workbook.write(fileOutputStream);
            }
            workbook.close();
            return filePath;
        }

        log.error("Error!");
        return null;
    }

    @Transactional
    public void customerReport(CustomerReportPayload payload) {
        CustomerReport report = reportRepository.findByAccountIdAndType(payload.getAccountId(), payload.getType()).orElseGet(() ->
                {
                    CustomerReport customerReport = new CustomerReport();
                    customerReport.setAccountId(payload.getAccountId());
                    customerReport.setCardNumber(payload.getCardNumber());
                    customerReport.setType(payload.getType());
                    customerReport.setAmount(0.0);
                    customerReport.setCreatedAt(LocalDateTime.now());
                    return customerReport;
                });

        report.setAmount(report.getAmount() + payload.getAmount());
        report.setCreatedAt(LocalDateTime.now());

        reportRepository.save(report);
    }
}
