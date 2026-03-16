package com.vaistra.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceItemDTO {

    private Integer productId;

    private double quantity;
    private double rate;

    private Double royalty;
    private Double dmf;
    private Double nmet;

    private Double royaltyAmount;
    private Double dmfAmount;
    private Double nmetAmount;

    private double taxableAmount;
    private double cgstPercent;
    private double sgstPercent;
    private double cgstAmount;
    private double sgstAmount;
    private double igstPercent;
    private double igstAmount;

    private ProductDto product;

}
