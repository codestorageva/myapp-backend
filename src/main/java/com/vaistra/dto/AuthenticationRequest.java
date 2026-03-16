package com.vaistra.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationRequest {
    @NotEmpty(message = "Email should not be empty!")
    @NotNull(message = "Email should not be null!")
    @Email(message = "Enter Valid Email")
    private String email;

    @NotEmpty(message = "Password should not be empty!")
    @NotNull(message = "Password should not be null!")
    private String password;
}