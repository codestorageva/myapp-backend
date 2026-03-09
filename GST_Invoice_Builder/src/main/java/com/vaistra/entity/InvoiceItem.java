package com.vaistra.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "invoice_item")
public class InvoiceItem {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer itemId;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "invoice_id")
        private InvoiceGenerator invoice;


        @ManyToOne
        @JoinColumn(name = "product_id")
        private Product product;


        private double quantity;
        private double rate;

    private double baseAmount;

    @Column(nullable = false)
    private double royalty;
    @Column(nullable = false)
    private double dmf;
    @Column(nullable = false)
    private double nmet;

    @Column(nullable = false)
    private double royaltyAmount;
    @Column(nullable = false)
    private double dmfAmount;
    @Column(nullable = false)
    private double nmetAmount;



    private double taxableAmount;
        private double cgstPercent;
        private double sgstPercent;
        private double cgstAmount;
        private double sgstAmount;
        private double igstPercent;
        private double igstAmount;


}
