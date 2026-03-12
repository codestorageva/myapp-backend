package com.vaistra.dto.update;

import com.vaistra.dto.BankDetailsDto;
import com.vaistra.dto.InvoiceItemDTO;
import com.vaistra.dto.OtherChargeDto;
import com.vaistra.dto.ProductDto;
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
public class InvoiceUpdateDto {

    private Integer invoiceId;
    private String invoicePrefix;
    private String invoiceNumber;
    private String invoiceDate;
    private String terms;
    private String dueDate;
    private Integer customerId;
    private String customerName;
    private String paymentMode;
    private String narration;

    private String lrNumber;
    private String transport;
    private String commissionerate;
    private String range;
    private String division;

    private List<InvoiceItemDTO> items;
    private List<OtherChargeDto> otherChargeDtos;

    private Double totalTaxableAmount;
    private Double totalCgst;
    private Double totalSgst;
    private Double totalIgst;
    private Double roundOff;
    private Double grandTotal;

    private Boolean status;
    private Boolean isDeleted;
    private Boolean isRcm;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    private Integer companyId; // Linked to Company
    private List<ProductDto> products;

}

