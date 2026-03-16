package com.vaistra.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AddressDto {


    private String attention;
    private String addressLine1;
    private String addressLine2;
    private String pincode;

}
