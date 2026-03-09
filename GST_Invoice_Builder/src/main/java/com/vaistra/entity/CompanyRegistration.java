package com.vaistra.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name="company_registration")

public class CompanyRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_id")
    private Integer companyId;

    private String companyName;
    private String ownerName;
    private String logo;
    private String password;
    private String mobileNumber;
    private String alternateMobileNumber;

    private String email;

    private String cinNumber;
//    private String lrNumber;
//    private String transport;
//    private String commissionerate;
//    private String range;
//    private String division;

    private String billingAddress1;
    private String billingAddress2;
    private String billingAddress3;
    private String billingPincode;
//
//    private String shippingAddress1;
//    private String shippingAddress2;
//    private String shippingAddress3;
//    private String shippingPincode;

    private String panNumber;
    private String gstNumber;
    private String industry;

    private Boolean status;
    private Boolean isDeleted;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    @ManyToOne
    @JoinColumn(name = "billing_state_id")
    private State billingState;

    @ManyToOne
    @JoinColumn(name = "billing_city_id")
    private City billingCity;

    @ManyToOne
    @JoinColumn(name = "shipping_state_id")
    private State shippingState;

    @ManyToOne
    @JoinColumn(name = "shipping_city_id")
    private City shippingCity;


    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "companyRegistration")
    private List<BankDetails> bankDetails = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "companyRegistration")
    private List<InvoiceGenerator> invoices = new ArrayList<>();

}
