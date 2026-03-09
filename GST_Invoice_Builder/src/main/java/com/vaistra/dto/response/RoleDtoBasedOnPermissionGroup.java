package com.vaistra.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class RoleDtoBasedOnPermissionGroup {
    private Integer roleId;

    private String roleName;

    private String guardName;

    private Boolean isDeleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    private List<PermissionSuperGroupResponse> permissionBasedOnSuperGroup = new ArrayList<>();
}
