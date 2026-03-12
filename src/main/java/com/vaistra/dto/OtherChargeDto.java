package com.vaistra.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OtherChargeDto {

    private Integer otherChargeId;
    private String label;
    private Double value;
    private Integer invoiceId;


}
