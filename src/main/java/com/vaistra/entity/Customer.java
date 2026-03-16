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
@Table(name = "customer")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Integer customerId;

    private String vid;
    private String customerType; // Business or Individual
    private String salutation;
    private String firstName;
    private String lastName;
    private String customerCompanyName;
    private String displayName;
    private String email;
    private String workPhone;
    private String mobileNumber;
    private String gstNumber;
    private String pan;
    private String terms;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "attention", column = @Column(name = "billing_attention")),
            @AttributeOverride(name = "addressLine1", column = @Column(name = "billing_address_line1")),
            @AttributeOverride(name = "addressLine2", column = @Column(name = "billing_address_line2")),
            @AttributeOverride(name = "pincode", column = @Column(name = "billing_pincode"))
    })
    private Address billingAddress;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "attention", column = @Column(name = "shipping_attention")),
            @AttributeOverride(name = "addressLine1", column = @Column(name = "shipping_address_line1")),
            @AttributeOverride(name = "addressLine2", column = @Column(name = "shipping_address_line2")),
            @AttributeOverride(name = "pincode", column = @Column(name = "shipping_pincode"))
    })
    private Address shippingAddress;


    @ManyToOne
    @JoinColumn(name = "billing_city_id")
    private City billingCity;

    @ManyToOne
    @JoinColumn(name = "billing_state_id")
    private State billingState;

    @ManyToOne
    @JoinColumn(name = "shipping_city_id")
    private City shippingCity;

    @ManyToOne
    @JoinColumn(name = "shipping_state_id")
    private State shippingState;

    // Place of Supply
    @ManyToOne
    @JoinColumn(name = "place_of_supply_state_id")
    private State placeOfSupply;


    private boolean sameAsBillingAddress; // for UI checkbox

    private Boolean status;

    private Boolean isDeleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<ContactPerson> contactPersons;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private CompanyRegistration companyRegistration;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "customer")
    private List<InvoiceGenerator> invoices = new ArrayList<>();


}
