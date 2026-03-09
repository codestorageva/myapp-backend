package com.vaistra.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor

public class ForgotPasswordDTO {

    private Integer id;

    @NotEmpty(message = "Email Should not be Empty!")
    @NotNull(message = "Email Should not be Null!")
    @Email(message = "Invalid Email!")
    private String email;
}
