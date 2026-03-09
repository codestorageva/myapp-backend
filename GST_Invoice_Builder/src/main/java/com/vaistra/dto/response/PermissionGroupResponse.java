package com.vaistra.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PermissionGroupResponse {
    private String permissionGroupName;
    List<?> permissionData = new ArrayList<>();  // Correct type for permission data
}
