package com.vaistra.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "invoice_generator")
public class InvoiceGenerator {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invoice_id")
    private Integer invoiceId;

    private String invoicePrefix;
    private String invoiceNumber;
    private String invoiceDate;
    private String terms;
    private String dueDate;
    private String paymentMode;
    private String narration;

    private double totalTaxableAmount;
    private double totalCgst;
    private double totalSgst;
    private double totalIgst;
    private Double roundOff;
    private double grandTotal;

    private Double paymentReceived;
    private Double closingBalance;

    private Boolean status;

    private String cinNumber;
    private String lrNumber;
    private String transport;
    private String commissionerate;
    private String range;
    private String division;

    private Boolean isDeleted;
    private Boolean isRcm;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private CompanyRegistration companyRegistration;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OtherCharges> otherCharges = new ArrayList<>();

}

