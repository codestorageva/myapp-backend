package com.vaistra.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class PermissionsBasedOnRoleDTO {
    private Integer permissionId;
    private String permissionSuperGroup;
    private String permissionGroup;
    private String permissionName;
    private String guardName;
}
