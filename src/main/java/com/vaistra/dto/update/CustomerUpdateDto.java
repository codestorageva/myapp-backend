package com.vaistra.dto.update;

import com.vaistra.dto.AddressDto;
import com.vaistra.dto.ContactPersonDTO;
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
public class CustomerUpdateDto {

    private Integer customerId;
    private String vid;
    private String customerType;
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

    // Billing Address (excluding city/state)
    private String billingAttention;
    private String billingAddressLine1;
    private String billingAddressLine2;
    private String billingPincode;

    private Integer billingCityId;
    private Integer billingStateId;

    private Integer placeOfSupplyStateId;
    private String placeOfSupplyStateName;



    // Shipping Address (excluding city/state)
    private String shippingAttention;
    private String shippingAddressLine1;
    private String shippingAddressLine2;
    private String shippingPincode;

    private Integer shippingCityId;
    private Integer shippingStateId;

    private boolean sameAsBillingAddress;

    private Boolean status;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    private List<ContactPersonDTO> contactPersons;

    private Integer companyId;
}
