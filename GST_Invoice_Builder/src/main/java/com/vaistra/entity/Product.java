package com.vaistra.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Integer productId;

    private String productName;

    private String type;

    private String hsnCode;

    private String sacCode;

    private String unit;

    private String taxPreference;

    private Integer quantity;

    private Double sellingPrice;

    private Double rate;

    private Double taxValue;

    private String gstPercent;

    private Double cgstAmount;

    private Double sgstAmount;

    private Double igstAmount;

    private Double netAmount;

    private String description;

    private Double royalty;
    private Double dmf;
    private Double nmet;
    private Boolean miningProduct;


    private Boolean status;

    private Boolean isDeleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

//    @ManyToOne
//    @JoinColumn(name = "invoice_id")
//    private InvoiceGenerator invoice;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private CompanyRegistration companyRegistration;




}
