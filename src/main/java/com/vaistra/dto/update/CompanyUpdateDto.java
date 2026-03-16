package com.vaistra.dto.update;


import com.vaistra.dto.BankDetailsDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CompanyUpdateDto{


    private String companyName;
    private String ownerName;
    private String logo;
    private String password;
    private String billingAddress1;
    private String billingAddress2;
    private String billingAddress3;
    private String billingState;
    private String billingCity;
    private String billingPincode;
    private String shippingAddress1;
    private String shippingAddress2;
    private String shippingAddress3;
    private Integer billingCityId;
    private Integer billingStateId;
    private Integer shippingCityId;
    private Integer shippingStateId;
    private String shippingPincode;
    private String panNumber;
    private String gstNumber;
    private String mobileNumber;
    private String industry;
    private Boolean status;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private List<BankDetailsDto> bankDetails;
}

