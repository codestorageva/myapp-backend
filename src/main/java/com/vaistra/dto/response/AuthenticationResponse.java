package com.vaistra.dto.response;

import lombok.*;
import org.springframework.http.HttpStatus;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthenticationResponse {

    Boolean success;
    HttpStatus successCode;
    String email;
    Integer roleId;
    String roleName;
    String fullName;
    String userName;
    String mobNo;
    String authToken;
    List<PermissionMappedDTO> permissionList;

}
