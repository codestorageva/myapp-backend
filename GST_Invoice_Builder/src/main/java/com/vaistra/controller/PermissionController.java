package com.vaistra.controller;

import com.vaistra.dto.PermissionDto;
import com.vaistra.dto.response.*;
import com.vaistra.dto.update.PermissionUpdateDto;
import com.vaistra.service.PermissionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("auth/permission")
public class PermissionController
{
    private final PermissionService permissionService;
//    private final AwsService awsService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;

    }

    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
    @PostMapping
    public ResponseEntity<MessageResponse> addPermission(@Valid @RequestBody PermissionDto permissionDto)
    {
        return new ResponseEntity<>(permissionService.addPermission(permissionDto), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
    @GetMapping("{permissionId}")
    public ResponseEntity<DataResponse> getPermissionById(@PathVariable int permissionId)
    {
        return new ResponseEntity<>(permissionService.getPermissionById(permissionId), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
    @GetMapping
    public ResponseEntity<HttpResponse> getAllPermissions(@RequestParam(value = "keyword", required = false) String keyword,
                                                          @RequestParam(value = "pageNumber", defaultValue = "0", required = false) Integer pageNumber,
                                                          @RequestParam(value = "pageSize", defaultValue = "2147483647", required = false) Integer pageSize,
                                                          @RequestParam(value = "sortBy", defaultValue = "updatedAt", required = false) String sortBy,
                                                          @RequestParam(value = "sortDirection", defaultValue = "asc", required = false) String sortDirection,
                                                          @RequestParam(value = "isDeleted", defaultValue = "false", required = false) String isDeleted) {

        return new ResponseEntity<>(permissionService.getAllPermissions(keyword, pageNumber, pageSize, sortBy, sortDirection, isDeleted), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
    @PutMapping("{permissionId}")
    public ResponseEntity<MessageResponse> updatePermission(@Valid @RequestBody PermissionUpdateDto permissionUpdateDto, @PathVariable int permissionId) {
        return new ResponseEntity<>(permissionService.updatePermission(permissionUpdateDto, permissionId), HttpStatus.OK);
    }


    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
    @PutMapping("softDelete/{permissionId}")
    public ResponseEntity<MessageResponse> softDeleteById(@PathVariable int permissionId) {
        return new ResponseEntity<>(permissionService.softDeletePermissionById(permissionId), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
    @PutMapping("restore/{permissionId}")
    public ResponseEntity<MessageResponse> restorePermissionById(@PathVariable int permissionId) {
        return new ResponseEntity<>(permissionService.restorePermissionById(permissionId), HttpStatus.OK);
    }

    //    @PreAuthorize("hasRole('ROLE_SUPERADMIN') or hasAuthority('PERMISSION_READ')")
    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
    @GetMapping("/active")
    public ResponseEntity<ListResponse> getAllPermissionByNonSoftDeleted() {
        return new ResponseEntity<>(permissionService.getAllPermissionByNonSoftDeleted(), HttpStatus.OK);

    }

    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
    @GetMapping("/getAllPermissionsByPermissionGroup")
    public ResponseEntity<PermissionGroupResponseDto> getAllPermissionsByPermissionGroup(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "pageNumber", defaultValue = "0", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "2147483647", required = false) Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = "updatedAt", required = false) String sortBy,
            @RequestParam(value = "sortDirection", defaultValue = "asc", required = false) String sortDirection,
            @RequestParam(value = "isDeleted", defaultValue = "false", required = false) String isDeleted){
        return new ResponseEntity<>(permissionService.getAllPermissionsByPermissionGroup(keyword, pageNumber, pageSize, sortBy, sortDirection, isDeleted),HttpStatus.OK);
    }

    @GetMapping("/totalPermission")
    public ResponseEntity<LongResponse> getTotalPermission(){
        return new ResponseEntity<>(permissionService.getTotalPermission(), HttpStatus.OK);
    }
}
