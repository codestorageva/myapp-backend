package com.vaistra.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SendMobileOTPDTO {

    @NotNull(message = "Mobile No should not be Empty!")
    @NotEmpty(message = "Mobile No should not be null!")
    private String mobNo;

}
