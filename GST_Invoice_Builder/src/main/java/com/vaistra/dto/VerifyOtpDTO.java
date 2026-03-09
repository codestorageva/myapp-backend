package com.vaistra.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class VerifyOtpDTO {

    private Integer id;

    @NotEmpty(message = "OTP Should not be Empty!")
    @NotNull(message = "OTP Should not be Null!")
    @Min(value = 6, message = "Invalid OTP!")
    private String otp;

    @NotEmpty(message = "Email Should not be Empty!")
    @NotNull(message = "Email Should not be Null!")
    @Email(message = "Invalid Email!")
    private String email;

    @NotEmpty(message = "Password Should not be Empty!")
    @NotNull(message = "Password Should not be Null!")
    private String newPassword;



}