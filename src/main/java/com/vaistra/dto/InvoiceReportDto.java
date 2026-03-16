package com.vaistra.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InvoiceReportDto {

    private String date;
    private String particulars;
    private String invoiceType;
    private String invoiceNumber;
    private Double quantity;
    private String material;

    private String value;
    private String royaltyValue;
    private String dmf;
    private String nmet;
    private String totalTaxableValue;
    private String sgst;
    private String cgst;
    private String igst;
    private String total;
    private String roundOff;
    private String grandTotal;
    private String paymentReceived;
    private String closingBalance;


    private String narration;
}
