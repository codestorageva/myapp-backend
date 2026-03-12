package com.vaistra.controller;

import com.vaistra.dto.RoleDto;
import com.vaistra.dto.response.*;
import com.vaistra.dto.update.RoleUpdateDto;
import com.vaistra.entity.User;
import com.vaistra.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("auth/role")
public class RoleController
{
    private final RoleService roleService;

    public RoleController(RoleService roleService)  {
        this.roleService = roleService;
    }

    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
    @PostMapping
    public ResponseEntity<MessageResponse> addRole(@Valid @RequestBody RoleDto roleDto ) {
        return new ResponseEntity<>(roleService.addRole(roleDto), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
    @GetMapping("{roleId}")
    public ResponseEntity<DataResponse> getRoleById(@PathVariable int roleId) {
        return new ResponseEntity<>(roleService.getRoleById(roleId), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
    @GetMapping
    public ResponseEntity<HttpResponse> getAllRoles(@RequestParam(value = "keyword", required = false) String keyword,
                                                    @RequestParam(value = "pageNumber", defaultValue = "0", required = false) Integer pageNumber,
                                                    @RequestParam(value = "pageSize", defaultValue = "2147483647", required = false) Integer pageSize,
                                                    @RequestParam(value = "sortBy", defaultValue = "updatedAt", required = false) String sortBy,
                                                    @RequestParam(value = "sortDirection", defaultValue = "asc", required = false) String sortDirection,
                                                    @RequestParam(value = "isDeleted", defaultValue = "false", required = false) String isDeleted){

        return new ResponseEntity<>(roleService.getAllRoles(keyword, pageNumber, pageSize, sortBy, sortDirection, isDeleted), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
    @PutMapping("{roleId}")
    public ResponseEntity<MessageResponse> updateRole(@Valid @RequestBody RoleUpdateDto roleUpdateDto , @PathVariable int roleId) {
        return new ResponseEntity<>(roleService.updateRole(roleUpdateDto, roleId), HttpStatus.OK);
    }

    /*@PreAuthorize("hasRole('ROLE_SUPERADMIN')")
    @DeleteMapping("hardDelete/{roleId}")
    public ResponseEntity<MessageResponse> deleteRoleById(@PathVariable int roleId) {
        return new ResponseEntity<>(roleService.deleteRoleById(roleId), HttpStatus.OK);
    }*/

    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
    @PutMapping("softDelete/{roleId}")
    public ResponseEntity<MessageResponse> softDeleteById(@PathVariable int roleId) {
        return new ResponseEntity<>(roleService.softDeleteRoleById(roleId), HttpStatus.OK);
    }


    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
    @PutMapping("restore/{roleId}")
    public ResponseEntity<MessageResponse> restoreRoleById(@PathVariable int roleId) {
        return new ResponseEntity<>(roleService.restoreRoleById(roleId), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
    @GetMapping("/exportData")
    public ResponseEntity<ListResponse> exportedRoleData() {
        return new ResponseEntity<>(roleService.exportedRoleData(), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
    @GetMapping("/active")
    public ResponseEntity<ListResponse> getAllRoleByNonSoftDeleted() {
        return new ResponseEntity<>(roleService.getAllRoleByNonSoftDeleted(), HttpStatus.OK);

    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/permissionByCurrentUserRole")
    public ResponseEntity<ListResponse> getAllRoleByNonSoftDeletedAndCurrentUserRole(@AuthenticationPrincipal User loggedInUser) {
        return new ResponseEntity<>(roleService.getAllRoleByNonSoftDeletedAndCurrentUserRole(loggedInUser), HttpStatus.OK);

    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/checkPermissionByRole")
    public ResponseEntity<BooleanResponse> checkPermissionByRoleId(
            @AuthenticationPrincipal User loggedInUser,
            @RequestParam(value = "permissionGroup") String permissionGroup,
            @RequestParam(value = "permissionName", defaultValue = "read", required = false) String permissionName)
    {
        return new ResponseEntity<>(roleService.checkPermissionByRoleId(loggedInUser,permissionGroup,permissionName),HttpStatus.OK);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/checkPermission")
    public ResponseEntity<BooleanResponse> checkPermission(
            @AuthenticationPrincipal User loggedInUser,
            @RequestParam(value = "permissionName",required = true) String permissionName)
    {
        return new ResponseEntity<>(roleService.checkPermission(loggedInUser, permissionName),HttpStatus.OK);
    }
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/checkAuth")
    public ResponseEntity<BooleanResponse> checkAuth(@AuthenticationPrincipal User loggedInUser){
        return new ResponseEntity<>(roleService.checkAuth(loggedInUser),HttpStatus.OK);
    }
    @GetMapping("/totalRole")
    public ResponseEntity<LongResponse> getTotalRole(){
        return new ResponseEntity<>(roleService.getTotalRole(), HttpStatus.OK);
    }


}

