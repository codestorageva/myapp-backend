package com.vaistra.dto;

import com.vaistra.dto.response.PermissionResponseDto;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class RoleDto {

    private Integer roleId;

    @NotEmpty(message = "Role name Should not be Empty!")
    @NotNull(message = "Role name should not be null!")
    private String roleName;

    private String guardName;

    private Boolean isDeleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    private List<PermissionResponseDto> permissions = new ArrayList<>();

}
