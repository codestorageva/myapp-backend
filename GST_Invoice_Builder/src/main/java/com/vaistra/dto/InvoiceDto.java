package com.vaistra.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceDto {

    private Integer invoiceId;
    private String invoicePrefix;
    private String invoiceNumber;
    private String invoiceDate;
    private String terms;
    private String dueDate;
    private String paymentMode;
    private String narration;


    private String lrNumber;
    private String transport;
    private String commissionerate;
    private String range;
    private String division;
    private List<InvoiceItemDTO> items;
    private List<OtherChargeDto> otherCharge;

    private double totalTaxableAmount;
    private double totalCgst;
    private double totalSgst;
    private double totalIgst;
    private Double roundOff;
    private double grandTotal;

    private Boolean status;


    private Boolean isDeleted;
    private Boolean isRcm;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    @NotNull(message = "Company ID is required.")
    private Integer companyId; // Linked to Company

    @NotNull(message = "Customer ID is required.")
    private Integer customerId;

    private CustomerDto customer; // Add this line to hold customer details

}
