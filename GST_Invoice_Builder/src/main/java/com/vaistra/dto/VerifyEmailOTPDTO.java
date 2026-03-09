package com.vaistra.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
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

public class VerifyEmailOTPDTO {
    @NotEmpty(message = "OTP Should not be Empty!")
    @NotNull(message = "OTP Should not be Null!")
    @Min(value = 6, message = "Invalid OTP!")
    private String otp;

    @NotEmpty(message = "Email Should not be Empty!")
    @NotNull(message = "Email Should not be Null!")
    @Email(message = "Invalid Email!")
    private String email;

}