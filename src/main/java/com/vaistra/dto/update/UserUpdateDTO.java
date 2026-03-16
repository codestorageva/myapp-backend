package com.vaistra.dto.update;

import jakarta.validation.constraints.Email;
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

public class UserUpdateDTO {
    private Integer userId;

    @Size(min = 15, message = "VID should be minimum 15 characters")
    private String vid;

    private String entityName;

    private String fullName;

    private String displayName;

    @Email(message = "Invalid Email!")
    private String email;

    private String password;

    private String uniqueName;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dob;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfCorporation;

    private  String mobNo;

    private String addressLine1;

    private String addressLine2;

    private String addressLine3;

    private String message;

    private Integer roleId;

    private String roleName;

    private Boolean isDeleted;

    private Boolean activeStatus;

    private Boolean isDocProvided;

    private String approvalStatus;

    private String approvedBy;

    private LocalDateTime approvedAt;

    private String rejectedBy;

    private LocalDateTime rejectedAt;

    private String rejectionReason;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;
}
