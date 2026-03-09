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
public class PermissionSuperGroupResponse {
    private String permissionSuperGroupName;  // Adjust the name to match the JSON structure
    List<?> permissionGroups = new ArrayList<>();  // Adjusted field name
}
