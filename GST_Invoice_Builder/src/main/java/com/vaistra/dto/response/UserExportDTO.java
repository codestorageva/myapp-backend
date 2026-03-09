package com.vaistra.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserExportDTO
{
    private Integer userId;

    private String email;

    private String password;

    private String fullName;

    private  String moNo;

    private String addressLine1;

    private String addressLine2;

    private String addressLine3;

    private String roleName;

    private String updatedAt;
}

