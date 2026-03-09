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
public class SendEmailOTPDTO {

    @NotEmpty(message = "Email Should not be Empty!")
    @NotNull(message = "Email should not be null!")
    private String email;

}
