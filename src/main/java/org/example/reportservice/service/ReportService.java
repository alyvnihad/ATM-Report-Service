package org.example.reportservice.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;

import org.example.reportservice.dto.ReportRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Service class responsible for generating PDF reports
 * containing customer account and card details after registration.
 */

@Service
@RequiredArgsConstructor
public class ReportService {
    @Value("${folder.path}")
    private String folderPath;

    // Generates a PDF document containing the customer's account and card information.
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
}
