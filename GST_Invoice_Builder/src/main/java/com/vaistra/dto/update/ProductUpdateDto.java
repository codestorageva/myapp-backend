package com.vaistra.dto.update;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductUpdateDto {

    private Integer productId;
    private String productName;
    private String type;
    private String hsnCode;
    private String sacCode;
    private String unit;
    private String taxPreference;
    private Double sellingPrice;
    private Integer quantity;
    private Double rate;
    private Double taxValue;
    private String gstPercent;
    private Double cgstAmount;
    private Double sgstAmount;
    private Double igstAmount;
    private Double netAmount;
    private String description;

    private Boolean status;
    private Boolean isDeleted;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    private Integer invoiceId;
    private Integer companyId;

    private Boolean miningProduct;  // Whether this is a mining product
    private Double royalty;         // Applicable if miningProduct = true
    private Double dmf;             // Applicable if miningProduct = true
    private Double nmet;            // Applicable if miningProduct = true
}

