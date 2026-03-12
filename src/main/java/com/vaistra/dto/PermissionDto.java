package com.vaistra.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PermissionDto {

    private Integer permissionId;

    @NotEmpty(message = "Permission SuperGroup not be Empty!")
    @NotNull(message = "Permission SuperGroup not be null!")
    private String permissionSuperGroup;

    @NotEmpty(message = "Permission Group not be Empty!")
    @NotNull(message = "Permission Group not be null!")
    private String permissionGroup;

    @NotEmpty(message = "Permission Should not be Empty!")
    @NotNull(message = "Permission should not be null!")
    private String permissionName;

    private String guardName;

    private Boolean isDeleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;
}
