package com.vaistra.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Integer userId;



    @NotNull(message = "FullName should not be Empty!")
    @NotEmpty(message = "FullName should not be null!")
    private String fullName;


    @NotEmpty(message = "Email Should not be Empty!")
    @NotNull(message = "Email should not be null!")
    private String email;

    @NotNull(message = "Password Name should not be null!")
    @NotEmpty(message = "Password Should not be Empty!")
    private String password;


    //    @NotNull(message = "Date of Birth should not be null!")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dob;


    @NotNull(message = "Mobile No should not be Empty!")
    @NotEmpty(message = "Mobile No should not be null!")
    private String mobNo;

    @NotNull(message = "AddressLine1 should not be null!")
    @NotEmpty(message = "AddressLine1 Should not be Empty!")
    private String addressLine1;

    private String addressLine2;

    private String addressLine3;

    private String message;

    private Integer roleId;

    private String roleName;

    private Boolean isDeleted;

    private Boolean activeStatus;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;
}