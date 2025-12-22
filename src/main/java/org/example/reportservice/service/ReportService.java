package org.example.reportservice.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPTable;
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
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
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

    // Generate Daily Log pdf
    @Transactional
    public String dailyLog(LocalDate date, TransactionRequest request) throws IOException {
        request.setDate(date);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(request.getRefreshToken());
        HttpEntity<TransactionRequest> entity = new HttpEntity<>(request, httpHeaders);

        ResponseEntity<TransactionResponse[]> response =
                restTemplate.postForEntity(transactionUrl + "/daily-log/" + date, entity, TransactionResponse[].class);

        TransactionResponse[] responses = response.getBody();
        if (responses == null || responses.length == 0) {
            log.error("Error! Response is empty.");
            return null;
        }

        List<TransactionResponse> list = Arrays.asList(responses);
        String folderPath = this.folderPath;
        File file = new File(folderPath);
        if (file.exists()) {
            file.mkdirs();
        }
        if (response.getBody() != null) {

            String filePath = folderPath + "\\Daily-log sheet-" + date + ".xlsx";
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
                row.createCell(2).setCellValue(result.getCardNumber().toString());
                row.createCell(3).setCellValue(result.getAmount());
                row.createCell(4).setCellValue(result.getType());
                row.createCell(5).setCellValue(result.getCreatedAt().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")));
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

    // Creates or updates customer report by accumulating transaction amount
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

    // Generate monthly PDF report for top 10 customers by transaction amount
    @Transactional
    public String topAmountMonthly(YearMonth month) {
        try {
            String folderPath = this.folderPath;
            File file = new File(folderPath);
            if (!file.exists()) {
                file.mkdirs();
            }

            String filePath = folderPath + "\\Customer reports Top amount monthly-" + month + ".pdf";

            LocalDateTime start = month.atDay(1).atStartOfDay();
            LocalDateTime end = month.atEndOfMonth().atTime(23, 59, 59);

            List<CustomerReport> reports = reportRepository.findTop10ByCreatedAtBetweenOrderByAmountDesc(start, end);
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();
            Paragraph Headers = new Paragraph("X Bank \n\n", FontFactory.getFont(FontFactory.COURIER_BOLD, 26, BaseColor.DARK_GRAY));
            Headers.setAlignment(Element.ALIGN_CENTER);
            document.add(Headers);
            Paragraph title = new Paragraph("Customer Reports\n" +
                    "Period: " + start + " to " + end + "\n\n" + "Table:\n\n",
                    FontFactory.getFont(FontFactory.COURIER_BOLD, 14, BaseColor.BLACK));
            document.add(title);
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1f, 3f, 2f, 2f, 1.5f});

            table.addCell("ID");
            table.addCell("Card Number");
            table.addCell("Amount");
            table.addCell("Date");
            table.addCell("Type");

            for (CustomerReport report : reports) {
                table.addCell(report.getAccountId().toString());
                table.addCell(report.getCardNumber().toString());
                table.addCell(report.getAmount().toString());
                table.addCell(report.getCreatedAt().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")));
                table.addCell(report.getType());
            }

            document.add(table);
            document.close();

            return filePath;
        } catch (Exception e) {
            log.error("Error fetching top reports for month {}", month, e);
            throw new RuntimeException("Error");
        }
    }
}
