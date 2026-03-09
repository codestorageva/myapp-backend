package com.vaistra.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RoleExportDTO
{
    private Integer roleId;
    private String roleName;
    private String guardName;
    private String updatedAt;
}

