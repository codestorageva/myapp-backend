package com.vaistra.dto.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PermissionMappedDTO {
    private Integer permissionId;
    private String permissionName;
}
