package com.vaistra.service;

import com.vaistra.entity.InvoiceGenerator;
import com.vaistra.entity.InvoiceItem;
import com.vaistra.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.util.List;



import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceReportService {

    private final InvoiceRepository invoiceRepository;

    public byte[] generateInvoiceReport() throws Exception {

        List<InvoiceGenerator> invoices = invoiceRepository.findAllWithDetails();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Invoice Report");

        int rowIndex = 0;

        // Header Row
        Row header = sheet.createRow(rowIndex++);
        String[] columns = {
                "Date", "Company ID", "Invoice Number", "Party Name", "Opposite Account",
                "Material", "GST Rate", "Quantity", "Rate", "Value",
                "Royalty In Amount", "DMF", "NMET",
                "Total Taxable Amount", "IGST", "CGST", "SGST",
                "Adjustment", "Grand Total", "Payment Received", "Closing Balance"
        };

        for (int i = 0; i < columns.length; i++) {
            header.createCell(i).setCellValue(columns[i]);
        }

        // Data Rows
        for (InvoiceGenerator invoice : invoices) {
            for (InvoiceItem item : invoice.getItems()) {

                Row row = sheet.createRow(rowIndex++);

                row.createCell(0).setCellValue(invoice.getInvoiceDate().toString());

                // ------------------ Company ID ------------------
                row.createCell(1).setCellValue(invoice.getCompanyRegistration().getCompanyId());

                row.createCell(2).setCellValue(invoice.getInvoicePrefix() + invoice.getInvoiceNumber());
                row.createCell(3).setCellValue(invoice.getCustomer().getDisplayName());
                row.createCell(4).setCellValue(invoice.getCompanyRegistration().getCompanyName());
                row.createCell(5).setCellValue(item.getProduct().getProductName());

                double gstRate = item.getIgstPercent() > 0 ?
                        item.getIgstPercent() :
                        item.getCgstPercent() + item.getSgstPercent();
                row.createCell(6).setCellValue(gstRate);

                row.createCell(7).setCellValue(item.getQuantity());
                row.createCell(8).setCellValue(item.getRate());
                row.createCell(9).setCellValue(item.getBaseAmount());
                row.createCell(10).setCellValue(item.getRoyaltyAmount());
                row.createCell(11).setCellValue(item.getDmfAmount());
                row.createCell(12).setCellValue(item.getNmetAmount());
                row.createCell(13).setCellValue(item.getTaxableAmount());
                row.createCell(14).setCellValue(item.getIgstAmount());
                row.createCell(15).setCellValue(item.getCgstAmount());
                row.createCell(16).setCellValue(item.getSgstAmount());
                row.createCell(17).setCellValue(invoice.getRoundOff());
                row.createCell(18).setCellValue(invoice.getGrandTotal());

                double paymentReceived = 0; // future: fetch from payment table
                row.createCell(19).setCellValue(paymentReceived);
                row.createCell(20).setCellValue(invoice.getGrandTotal() - paymentReceived);
            }
        }

        // Auto size columns
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();

        return bos.toByteArray();
    }
}
