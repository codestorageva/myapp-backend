package com.vaistra.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CompanyDto {

    private Integer companyId;
    private String companyName;
    private String ownerName;
    private String logo;
    private String password;

    private String billingAddress1;
    private String billingAddress2;
    private String billingAddress3;
    private String billingPincode;
    private Integer billingCityId;
    private String billingCityName;
    private Integer billingStateId;
    private String billingStateName;

    private String panNumber;
    private String gstNumber;
    private String mobileNumber;
    private String alternateMobileNumber;

    private String email;
    private String cinNumber;
//    private String lrNumber;
//    private String transport;
//    private String commissionerate;
//    private String range;
//    private String division;

    private String industry;

    private Boolean status;
    private Boolean isDeleted;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    private List<BankDetailsDto> bankDetails;
    private List<InvoiceDto> invoices;
}

