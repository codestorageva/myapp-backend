package com.vaistra.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
public class ProductDto {

    private Integer productId;
    private String productName;
    private String type;
    private String hsnCode;
    private String sacCode;
    private String unit;
    private String taxPreference;
    private Double sellingPrice;

    @Min(value = 1, message = "Quantity must be at least 1.")
    private Integer quantity;

    private Double rate;
    private Double taxValue;
    private String gstPercent;
    private Double cgstAmount;
    private Double sgstAmount;
    private Double igstAmount;
    private Double netAmount;
    private String description;

    private Boolean miningProduct;
    private Double royalty;
    private Double dmf;
    private Double nmet;

    private Boolean status;
    private Boolean isDeleted;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;


    @NotNull(message = "Company ID is required.")
    private Integer companyId;

}
