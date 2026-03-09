package com.vaistra.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "bank_details")

public class BankDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bank_id")
    private Integer bankId;

    private String bankName;

    private String accHolderName;

    private String ifscCode;

    private String branch;

    private String accountNumber;

    private String bankAddress;

    private Boolean status;

    private Boolean isDeleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private CompanyRegistration companyRegistration;
}
