package com.vaistra.dto.update;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OtherChargesUpdateDto {

    private Integer otherChargeId;
    private String label;
    private Double value;

    private Integer invoiceId;
}
