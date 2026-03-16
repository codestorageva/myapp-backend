package com.vaistra.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BankDetailsDto {

    private Integer bankId;
    private String bankName;
    private String ifscCode;
    private String branch;
    private String accountNumber;
    private String accHolderName;
    private String bankAddress;

    private Boolean status;
    private Boolean isDeleted;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    private Integer companyId;


}
